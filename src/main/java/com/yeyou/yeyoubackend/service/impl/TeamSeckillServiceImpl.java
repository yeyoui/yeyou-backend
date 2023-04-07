package com.yeyou.yeyoubackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.mapper.TeamSeckillMapper;
import com.yeyou.yeyoubackend.model.bo.TeamSeckillSyncBo;
import com.yeyou.yeyoubackend.model.domain.Team;
import com.yeyou.yeyoubackend.model.domain.TeamSeckill;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.domain.UserTeam;
import com.yeyou.yeyoubackend.model.request.TeamSeckillDelRequest;
import com.yeyou.yeyoubackend.model.request.TeamSeckillRequest;
import com.yeyou.yeyoubackend.model.vo.TeamUserSeckillVo;
import com.yeyou.yeyoubackend.model.vo.TeamUserVo;
import com.yeyou.yeyoubackend.service.TeamSeckillService;
import com.yeyou.yeyoubackend.service.TeamService;
import com.yeyou.yeyoubackend.service.UserTeamService;
import com.yeyou.yeyoubackend.utils.RedisIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.yeyou.yeyoubackend.contant.RedisConstant.TEAMSECKILL_INFO_KEY;
import static com.yeyou.yeyoubackend.contant.RedisConstant.TEAMSECKILL_ORDERID_KEY;

/**
* @author lhy
* @description 针对表【team_seckill(队伍抢夺表)】的数据库操作Service实现
* @createDate 2023-03-19 19:46:48
*/
@Service
public class TeamSeckillServiceImpl extends ServiceImpl<TeamSeckillMapper, TeamSeckill>
    implements TeamSeckillService {
    @Resource
    private TeamService teamService;
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private RedisTemplate<String,String> redisTemplate;
    @Resource
    private RedisIdWorker redisIdWorker;

    private static final ExecutorService SECKiLL_ORDER_EXECUTOR=new ThreadPoolExecutor(8,24,
            30, TimeUnit.SECONDS,new ArrayBlockingQueue<>(100));

    //订单监测和生成异步订单LUA脚本
    private static final DefaultRedisScript<Long> TEAMSECKILL_SCRIPT;
    //初始化
    static {
        TEAMSECKILL_SCRIPT=new DefaultRedisScript<>();
        TEAMSECKILL_SCRIPT.setLocation(new ClassPathResource("luaScript/teamSeckillOrder.lua"));
        TEAMSECKILL_SCRIPT.setResultType(Long.class);
    }

    //自动执行任务
    @PostConstruct
    private void init(){
        SECKiLL_ORDER_EXECUTOR.submit(new TeamSeckillOrderHandler());
    }

    private class TeamSeckillOrderHandler implements Runnable{
        //每次最多等待59秒后进入下一次循环
        private static final long waitTime = 1 * 60 * 1000 -1000;
        private static final String GROUP_KEY="stream.teamSeckillOrder";
        private static final String GROUP_NAME="TeamSeckillC1";
        private final String threadName = Thread.currentThread().getName();
        @Override
        public void run() {
            while(true){
                //1.获取消息
                try {
                    List<MapRecord<String, Object, Object>> mapRecordList = redisTemplate.opsForStream().read(
                            Consumer.from(GROUP_NAME,threadName),
                            StreamReadOptions.empty().count(1).block(Duration.ofMillis(waitTime)),
                            StreamOffset.create(GROUP_KEY, ReadOffset.lastConsumed())
                    );
                    //没有消息要处理
                    if(mapRecordList==null || mapRecordList.isEmpty()) continue;
                    //2.处理要获取的消息
                    MapRecord<String, Object, Object> record = mapRecordList.get(0);
                    Map<Object, Object> value = record.getValue();
                    //3.得到消息数据
                    TeamSeckillSyncBo teamSeckillSyncBo = BeanUtil.fillBeanWithMap(value, new TeamSeckillSyncBo(), true);
                    //4.处理消息
                    syncTeamSeckillResult(teamSeckillSyncBo);
                    //5.ACK确认处理完成
                    redisTemplate.opsForStream().acknowledge(GROUP_KEY,GROUP_NAME,record.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("线程 "+threadName+" 处理订单出现问题-->重试");
                    //重新处理
                    handlePendingList();
                }
            }
        }
        private void handlePendingList() {
            while(true){
                //1.获取消息
                try {
                    List<MapRecord<String, Object, Object>> mapRecordList = redisTemplate.opsForStream().read(
                            Consumer.from(GROUP_NAME,threadName),
                            StreamReadOptions.empty().count(1).block(Duration.ofMillis(waitTime)),
                            StreamOffset.create(GROUP_KEY, ReadOffset.from("0"))
                    );
                    //没有消息要处理
                    if(mapRecordList==null || mapRecordList.isEmpty()) break;
                    //2.处理要获取的消息
                    MapRecord<String, Object, Object> record = mapRecordList.get(0);
                    Map<Object, Object> value = record.getValue();
                    //3.得到消息数据
                    TeamSeckillSyncBo teamSeckillSyncBo = BeanUtil.fillBeanWithMap(value, new TeamSeckillSyncBo(), true);
                    //4.处理消息
                    syncTeamSeckillResult(teamSeckillSyncBo);
                    //5.ACK确认处理完成
                    redisTemplate.opsForStream().acknowledge(GROUP_KEY,GROUP_NAME,record.getId());
                    break;
                } catch (Exception e) {
                    log.error("线程 "+threadName+" 重新处理订单出现问题-->重试");
                    //重新处理
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    @Override
    @Transactional
    public Long addSeckill(TeamSeckillRequest teamSeckillRequest, User loginUser) {
        Long teamId = checkTeamSeckillRequest(teamSeckillRequest, loginUser);
        //队伍不能再争夺队列
        Long count = this.query().eq("teamId", teamId).count();
        if(count!=0){
            //新增操作的时候发现队伍存在争夺队列中，当做更新操作处理
            return updateSeckill(teamSeckillRequest,loginUser);
        }
        //新增队伍到争夺列表
        TeamSeckill teamSeckill = new TeamSeckill();
        BeanUtils.copyProperties(teamSeckillRequest,teamSeckill);
        boolean result = this.save(teamSeckill);
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        //修改队伍状态为争夺状态
        result = teamService.update().set("status", 3).eq("id", teamId).update();
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR);

        //将争夺信息和队伍加入Redis缓存中
        String redisKey=TEAMSECKILL_INFO_KEY+teamId;
        redisTemplate.opsForValue().set(redisKey,teamSeckillRequest.getJoinNum().toString());
        //设置过期时间为结束时间
        redisTemplate.expireAt(redisKey, teamSeckillRequest.getEndTime());
        //缓存队伍信息
        Gson gson = new Gson();
        TeamUserSeckillVo teamUserSeckillVo = packageOneTeamUserSeckillVo(teamSeckill, loginUser.getId());
        redisTemplate.opsForHash().put(RedisConstant.TEAMSECKILL_TEAMINFO_HASH,teamId.toString(),gson.toJson(teamUserSeckillVo));
        return teamId;
    }

    @Override
    @Transactional
    public Long updateSeckill(TeamSeckillRequest teamSeckillRequest, User loginUser) {
        Long teamId = checkTeamSeckillRequest(teamSeckillRequest, loginUser);
        //队伍已必须在争夺队列
        TeamSeckill teamSeckill = this.query().select("id").eq("teamId", teamId).one();
        if(teamSeckill==null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在争夺队列中");
        //修改争夺列表的队伍
        BeanUtils.copyProperties(teamSeckillRequest,teamSeckill);
        teamSeckill.setId(teamSeckill.getId());

//        UpdateWrapper<TeamSeckill> teamSeckillUpdateWrapper = new UpdateWrapper<>();
//        teamSeckillUpdateWrapper.eq("teamId", teamId)
//                .set("joinNum",teamSeckill.getJoinNum())
//                .set("beginTime",teamSeckill.getBeginTime())
//                .set("endTime",teamSeckill.getEndTime());
        boolean result = this.updateById(teamSeckill);
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        //更新Redis缓存
        //更新后的TeamSeckill信息
        Gson gson = new Gson();
        TeamUserSeckillVo teamUserSeckillVo = packageOneTeamUserSeckillVo(teamSeckill, loginUser.getId());
        redisTemplate.opsForHash().put(RedisConstant.TEAMSECKILL_TEAMINFO_HASH,teamId.toString(),gson.toJson(teamUserSeckillVo,TeamUserSeckillVo.class));

        return teamId;
    }

    @Override
    public List<TeamUserSeckillVo> listAll(User loginUser) {
        //1.获取有效争夺状态的队伍ID
        Gson gson = new Gson();
        //Redis缓存
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(RedisConstant.TEAMSECKILL_TEAMINFO_HASH);
        List<TeamUserSeckillVo> seckillVoCollect = entries.values()
                .stream().map((valueJson) -> gson.fromJson((String) valueJson, TeamUserSeckillVo.class))
                .filter((team) -> {
                    boolean noExpire = team.getEndTime().after(new Date());
                    if(!noExpire){
                        //清除过期的队伍信息
                        redisTemplate.opsForHash().delete(RedisConstant.TEAMSECKILL_TEAMINFO_HASH,team.getId());
                    }
                    return noExpire;
                }).collect(Collectors.toList());

        //Mysql
//        List<TeamSeckill> teamSeckills = this.query().gt("endTime", new Date()).list();
//        if(teamSeckills==null) return new ArrayList<>();
//        List<TeamUserSeckillVo> seckillVoCollect = teamSeckills.stream().map((teamSeckill ->
//                packageOneTeamUserSeckillVo(teamSeckill, loginUser.getId()))).collect(Collectors.toList());

        return seckillVoCollect;
    }

    private TeamUserSeckillVo packageOneTeamUserSeckillVo(TeamSeckill teamSeckill,long loginUserId){
        TeamUserSeckillVo seckillVo = new TeamUserSeckillVo();
        //2通过队伍ID查询详细信息（可用Redis缓存）
        TeamUserVo teamUserVo = teamService.getTeamsById(teamSeckill.getTeamId(), loginUserId);
//            TeamUserVo teamUserVo = teamService.getTeamsById(teamSeckill.getTeamId(), 1);//测试用
        BeanUtils.copyProperties(teamUserVo, seckillVo);
        seckillVo.setJoinNum(teamSeckill.getJoinNum());
        seckillVo.setBeginTime(teamSeckill.getBeginTime());
        seckillVo.setEndTime(teamSeckill.getEndTime());
        return seckillVo;
    }

    @Override
    public boolean joinSeckillTeam(Long teamId, User loginUser) {
        //1. 校验参数是否正确
        //teamId和loginUser已经检查过了
        //2. 查询队伍信息
        TeamSeckill teamSeckill = this.query().eq("teamId", teamId).one();
        if(teamSeckill==null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        if(new Date().before(teamSeckill.getBeginTime())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "秒杀未开始！");
        }
        //3. 提前生成订单信息(使用类似雪花算法的方式生成,时间戳+Redis自增值)
        long orderId = redisIdWorker.nextId(TEAMSECKILL_ORDERID_KEY);
        //4. 执行lua脚本，查询Redis缓存中查看用户是否下单，如果没下单就减去库存，并在redis中新增信息
        Long result = redisTemplate.execute(
                TEAMSECKILL_SCRIPT,
                Collections.emptyList(),
                loginUser.getId().toString(), teamId.toString(), Long.toString(orderId));
        if(result==null) throw new BusinessException(ErrorCode.SYSTEM_ERROR);

        if (result == 1) {
            throw new BusinessException(ErrorCode.SECKILL_NORMAL_ERROR,"手速慢拉！");
        } else if (result == 2) {
            throw new BusinessException(ErrorCode.NO_AUTH, "不可重复下单");
        }
        //5. 如果lua脚本返回正确结果，将下单信息交给消息队列处理（暂时使用Redis的消息队列）
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteSeckillByTeamId(TeamSeckillDelRequest teamSeckillDelRequest, User loginUser) {
        //1. 检查参数
        if(teamSeckillDelRequest==null || teamSeckillDelRequest.getTeamId()==null ||
        teamSeckillDelRequest.getStatus()==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Long teamId = teamSeckillDelRequest.getTeamId();
        Integer status = teamSeckillDelRequest.getStatus();
        if(teamId<0 || status<0 ||status>3) throw new BusinessException(ErrorCode.PARAMS_ERROR,"信息输入错误");
        //2. 查询对应的队伍
        Team team = teamService.getById(teamId);
        TeamSeckill teamSeckill = this.query().select("id").eq("teamId", teamId).one();
        if(team==null || teamSeckill==null) throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        //3. 只有队长才能操作
        if(!Objects.equals(team.getLeaderId(), loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //4. 删除信息
        this.removeById(teamSeckill.getId());
        //5. 修改队伍状态为设定的状态
        boolean result = teamService.update().set("status", status).eq("id", teamId).update();
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        //删除Redis中的缓存
        redisTemplate.opsForHash().delete(RedisConstant.TEAMSECKILL_TEAMINFO_HASH, teamId.toString());
        return true;
    }

    private Long checkTeamSeckillRequest(TeamSeckillRequest teamSeckillRequest, User loginUser) {
        //1. 检查参数
        if(teamSeckillRequest ==null || teamSeckillRequest.getTeamId()<0 ||
                teamSeckillRequest.getJoinNum()==null || teamSeckillRequest.getJoinNum()<=0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //1.1校验时间
        if(teamSeckillRequest.getBeginTime().after(teamSeckillRequest.getEndTime())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"结束时间不能早于开始时间");
        }
        if(teamSeckillRequest.getEndTime().before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"结束时间不能早于当前时间");
        }
        Long teamId = teamSeckillRequest.getTeamId();
        Team team = teamService.getById(teamId);
        if(team==null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        //3. 只有队长才能操作
        if(!Objects.equals(team.getLeaderId(), loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //4. 校验队伍剩余人数是否足够
        if(team.getMaxNum()-team.getMemberNum()< teamSeckillRequest.getJoinNum()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍空间不足");
        }
        return teamId;
    }

    @Transactional
    public boolean syncTeamSeckillResult(TeamSeckillSyncBo teamSeckillSyncBo){
        if(teamSeckillSyncBo==null) return false;
        //更新队伍成员数量
        boolean result = teamService.update().setSql("memberNum=memberNum+1")
                .eq("id", teamSeckillSyncBo.getTeamId()).update();
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(teamSeckillSyncBo.getUserId());
        userTeam.setTeamId(teamSeckillSyncBo.getTeamId());
        //添加到队伍关系表
        result=userTeamService.save(userTeam);
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        return true;
    }

}




