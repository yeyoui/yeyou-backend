package com.yeyou.yeyoubackend.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yeyou.yeyoubackend.annotation.PriorityUser;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.contant.UserConstant;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.mapper.UserMapper;
import com.yeyou.yeyoubackend.model.vo.UserVo;
import com.yeyou.yeyoubackend.service.UserService;
import com.yeyou.yeyoubackend.utils.AlgorithmUtils;
import com.yeyou.yeyoubackend.utils.RedisIdWorker;
import com.yeyou.yeyoubackend.utils.StringRedisCacheUtils;
import com.yeyou.yeyoubackend.utils.UserHold;
import javafx.util.Pair;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yeyou.yeyoubackend.contant.UserConstant.USER_LOGIN_STATE;

/**
* @author lhy
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2023-03-13 12:41:58
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private StringRedisCacheUtils redisCacheUtils;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private RedisTemplate<String,Objects> redisTemplate;

    /**
     * 盐值
     */
    private static final String SALT = "yeyoui";
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());//MD5盐值加密
//        String encryptPassword=userPassword;//暂时不加密
        //新增用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        //默认角色
        user.setUserRole(UserConstant.DEFAULT_ROLE);
        //设置用户编号
        Long uid= redisTemplate.opsForValue().increment("IdIncr");
        String sUid;
        if(uid==null) sUid=UUID.randomUUID().toString();
        else sUid=uid.toString();
        user.setUserCode(sUid);
        user.setUsername("User"+uid);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
//        String encryptPassword = userPassword;
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
//        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserCode(originUser.getUserCode());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        redisTemplate.delete(RedisConstant.USER_TOKEN_KEY + UserHold.getToken());

        return 1;
    }
    /**
     * 根据标签搜索用户
     * @param tagList 标签列表
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagList) {
        if(CollectionUtils.isEmpty(tagList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //通过sql查询
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        //拼接and查询
//        //like '%Java%' and like ...
//        for (String tagName : tagList) {
//            queryWrapper=queryWrapper.like("tags",tagName);
//        }
//        List<User> users = userMapper.selectList(queryWrapper);
//        return users.stream().map(this::getSafetyUser).collect(Collectors.toList());
        //内存查询(更灵活)
        Gson gson = new Gson();
        //先查询所有有标签用户
        //逻辑过期（一小时）
        List<User> users =redisCacheUtils.queryWithLogicalExpireNoParam(RedisConstant.USER_ALL_USERTAGINFO_KEY,
                "ALL",
                RedisConstant.USER_ALL_USERTAGINFO_LOCK,
                ()-> this.query().ne("tags","[]").list(),
                RedisConstant.USER_ALL_USERTAGINFO_TTL, TimeUnit.HOURS);
        String json = gson.toJson(users);
        users=gson.fromJson(json,new TypeToken<List<User>>(){}.getType());
//        List<User> users = this.query().ne("tags","[]").list();
        return users.stream().filter((user -> {
            String tags = user.getTags();
            Set<String> tempTagsSet = gson.fromJson(tags, new TypeToken<Set<String>>() {}.getType());
            if(tempTagsSet==null) return false;
            for (String tag : tagList) {
                if(!tempTagsSet.contains(tag)) return false;
            }
            return true;
        })).map(user -> {
            //获取详细用户信息
            User userInfo = this.getById(user.getId());
            //脱敏
            return this.getSafetyUser(userInfo);
        }).collect(Collectors.toList());
    }

    /**
     * 获取当前登录的用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        Object curUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        User curUser = UserHold.get();
        if(curUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return curUser;
    }

    /**
     * 更新用户信息
     */
    @Override
    public int updateUserBySelf(User user, User currentUser) {
        Long id = user.getId();
        if(id<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //如果不是管理员且修改的不是自己的账号，抛出异常
        if(!Objects.equals(currentUser.getId(), id) && !isAdmin(currentUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //要更新的用户不存在
        if(this.getById(id)==null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return this.updateById(user) ?1:0;
    }

    /**
     * 通过User直接判断是否为管理员
     */
    @Override
    public boolean isAdmin(User currentUser) {
        return currentUser!=null && currentUser.getUserRole()== UserConstant.ADMIN_ROLE;
    }

    /**
     * 通过HttpServletRequest判断是否为管理员
     * (系统内部调用)
     */
    @Override
    public boolean isAdmin(HttpServletRequest request){
        if(request==null) return false;
//        Object user = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = UserHold.get();
        return user!=null && isAdmin(user);
    }


    @Override
    public List<User> cacheMathUsers(long num, User loginUser) {
        String tagsGson = loginUser.getTags();
        if (StringUtils.isBlank(tagsGson)) return null;
        Gson gson = new Gson();
        List<String> userTags = gson.fromJson(tagsGson, new TypeToken<List<String>>() {}.getType());
        //逻辑过期（一小时）
        List<User> userList =redisCacheUtils.queryWithLogicalExpireNoParam(RedisConstant.USER_ALL_USERTAGINFO_KEY,
                "ALL",
                RedisConstant.USER_ALL_USERTAGINFO_LOCK,
                ()-> this.query().select("id","tags").isNotNull("tags").list(),
                RedisConstant.USER_ALL_USERTAGINFO_TTL, TimeUnit.HOURS);
//        List<User> userList = this.query().select("id","tags").ne("tags","[]").list();
        if(CollectionUtils.isEmpty(userList)) return null;
        //存储用户标签的相似度 key:相似度分数（越小越接近） value: 用户id
        ArrayList<Pair<Long, Long>> userTagSimilar = new ArrayList<>(userList.size());
        String toJson = gson.toJson(userList);
        userList=gson.fromJson(toJson, new TypeToken<List<User>>(){}.getType());
        for (int i = 0; i < userList.size(); i++) {
            User newUser = userList.get(i);
            //剔除空标签和查询用户自己
            if(StringUtil.isBlank(newUser.getTags()) ||Objects.equals(newUser.getId(),loginUser.getId())) continue;
            //解析查询用户标签
            List<String> newUserTags = gson.fromJson(newUser.getTags(), new TypeToken<List<String>>() {
            }.getType());
            //计算分数
            long distance= AlgorithmUtils.minDistance(userTags,newUserTags);
            userTagSimilar.add(new Pair<>(distance,newUser.getId()));
        }
        //将所有用户标签相似度进行排序
        List<Long> mathUserRankIds = userTagSimilar.stream().
                sorted((a, b) -> (int) (a.getKey() - b.getKey()))
                .limit(num)
                .map(Pair::getValue)
                .collect(Collectors.toList());
        //查询用户详细信息
        if(mathUserRankIds.isEmpty()) return new ArrayList<>();
        String idStr = StrUtil.join(",", mathUserRankIds);
        List<User> matchList = this.query().in("id", mathUserRankIds).last("ORDER BY FIELD (id," + idStr + ")").list();
        matchList = matchList.stream().map(this::getSafetyUser).collect(Collectors.toList());
        return matchList;
    }

    @Override
    @PriorityUser
    public List<User> mathUsers(long num, User loginUser) {
        return cacheMathUsers(num, loginUser);
    }

    @Override
    public List<User> getRandomUser(int num) {
        if(num<0 || num>100) throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求数过多，最多为100");
        return userMapper.getRandomUser(num);
    }

    @Override
    public List<String> getMyTags(User loginUser) {
        User user = this.query().select("tags").eq("id", loginUser.getId()).one();
        String tags = user.getTags();
        if(StringUtils.isEmpty(tags)) return new ArrayList<>();
        Gson gson = new Gson();
        return gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
    }

    @Override
    public Boolean updMyTags(List<String> tags, User loginUser) {
        if(tags==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Gson gson = new Gson();
        String json = gson.toJson(tags, new TypeToken<List<String>>() {
        }.getType());
        User user = new User();
        user.setId(loginUser.getId());
        user.setTags(json);
        boolean result = this.updateById(user);
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        return true;
    }

    @Override
    public String randomUserIcon() {
        int ix = RandomUtil.randomInt(1, 21);
        return "http://yeapi.top/icons/default/("+ix+").png";
    }

    @Override
    public UserVo getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVo userVO = new UserVo();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVo> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }
}




