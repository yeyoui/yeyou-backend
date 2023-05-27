package com.yeyou.yeyoubackend.service.impl;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.contant.CommonConstant;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.exception.ThrowUtils;
import com.yeyou.yeyoubackend.model.domain.PostFavour;
import com.yeyou.yeyoubackend.model.domain.PostThumb;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.dto.post.PostQueryRequest;
import com.yeyou.yeyoucommon.constant.MqConstants;
import com.yeyou.yeyoucommon.model.domain.Post;
import com.yeyou.yeyoubackend.service.PostFavourService;
import com.yeyou.yeyoubackend.service.PostService;
import com.yeyou.yeyoubackend.mapper.PostMapper;
import com.yeyou.yeyoubackend.service.PostThumbService;
import com.yeyou.yeyoubackend.service.UserService;
import com.yeyou.yeyoubackend.utils.SqlUtils;
import com.yeyou.yeyoucommon.model.vo.PostVO;
import com.yeyou.yeyoucommon.model.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author lhy
 * @description 针对表【post(帖子)】的数据库操作Service实现
 * @createDate 2023-04-27 11:46:48
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Resource
    private UserService userService;
    @Resource
    private PostThumbService postThumbService;
    @Resource
    private PostFavourService postFavourService;
    @Resource
    private RabbitTemplate rabbitTemplate;

    private static final ExecutorService paramPoolServer=new ThreadPoolExecutor(2,2,
            30, TimeUnit.SECONDS,new ArrayBlockingQueue<>(1000));
    @Override
    public void validPost(Post post, boolean add) {
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        String title = post.getTitle();
        String content = post.getContent();
        if(add){
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title,content),ErrorCode.PARAMS_ERROR,"内容为空");
        }
        if(StringUtils.isNotBlank(title) && title.length() >128){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标题过长");
        }
        if(StringUtils.isNotBlank(content) && title.length() >8192){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"内容过长");
        }
    }

    @Override
    public long savePost(Post post, User user) {
        post.setUserId(user.getId());
        validPost(post, true);
        boolean save = this.save(post);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR);
        //同步ES
        Long postId = post.getId();
        rabbitTemplate.convertAndSend(MqConstants.POST_EXCHANGE,MqConstants.POST_INSERT_KEY,postId);
        return postId;
    }

    @Override
    public boolean updatePost(Post post, User user) {
        //参数校验
        validPost(post, false);
        Post oldPost = this.getById(post.getId());
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        //权限校验
        if(!oldPost.getUserId().equals(user.getId()) && userService.isAdmin(user)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //更新数据
        boolean success= this.updateById(post);
        ThrowUtils.throwIf(!success,ErrorCode.SYSTEM_ERROR);
        //同步ES
        rabbitTemplate.convertAndSend(MqConstants.POST_EXCHANGE,MqConstants.POST_INSERT_KEY,post.getId());
        return true;
    }

    @Override
    public PostVO getPostVO(Post post, User user) {
        //1.获取帖子包装类
        PostVO postVO = PostVO.objToVo(post);
        Long postId = postVO.getId();
        //2.关联查询用户信息
        Long userId = post.getUserId();
        User author = null;
        if (userId != null && userId > 0) {
            author = userService.getById(userId);
        }
        UserVo userVO = userService.getUserVO(author);
        postVO.setAuthor(userVO);
        //3.设置状态信息
        if(user!=null){
            //是否点赞过
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.eq("postId",postId);
            postThumbQueryWrapper.eq("userId", user.getId());
            long num= postThumbService.count(postThumbQueryWrapper);
            postVO.setHasThumb(num > 0);
            //是否收藏过
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.eq("postId",postId);
            postFavourQueryWrapper.eq("userId", user.getId());
            num= postFavourService.count(postFavourQueryWrapper);
            postVO.setHasThumb(num > 0);
        }
        return postVO;
    }

    @Override
    public Page<PostVO> listPostVoPage(PostQueryRequest postQueryRequest, User user) {
        int pageSize = postQueryRequest.getPageSize();
        int pageNum = postQueryRequest.getPageNum();
        Page<Post> postPage = new Page<>(pageNum,pageSize);
        postPage=this.page(postPage, getQueryWrapper(postQueryRequest));
        return this.getPostVoPage(postPage,user);
    }

    @Override
    public QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = postQueryRequest.getSearchText();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        Long userId = postQueryRequest.getUserId();
        Long notId = postQueryRequest.getNotId();
        //拼接查询条件
        if(StringUtils.isNotBlank(searchText)){
            queryWrapper.like("title", searchText).or().like("content", searchText);
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if(CollectionUtils.isNotEmpty(tagList)){
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<PostVO> getPostVoPage(Page<Post> postPage, User user) {
        List<Post> postList = postPage.getRecords();
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(),postPage.getPages(),postPage.getTotal());
        if(CollectionUtils.isEmpty(postList)){
            return postVOPage;
        }
        //1.关联查询用户信息（作者）
        Set<Long> userIdSet = postList
                .stream()
                .map(Post::getUserId)
                .collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService
                .listByIds(userIdSet)
                .stream()
                .collect(Collectors.groupingBy(User::getId));
        //2.获取状态（是否点赞，收藏）
        HashMap<Long, Boolean> postIdHasThumbMap = new HashMap<>();
        HashMap<Long, Boolean> postIdHasFavourMap = new HashMap<>();
        final long loginUserId = Optional.of(user.getId()).orElse(-1L);
        if(loginUserId!=-1){
            //postID集合
            Set<Long> postIdSet = postList.stream().map(Post::getId).collect(Collectors.toSet());

//            //串行执行
//            //获取点赞信息
//            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
//            postThumbQueryWrapper.in("postId", postIdSet);
//            postThumbQueryWrapper.eq("userId", loginUserId);
//            List<PostThumb> postThumbList = postThumbService.list(postThumbQueryWrapper);
//            postThumbList.forEach((postThumb -> postIdHasThumbMap.put(postThumb.getPostId(), true)));
//            //获取收藏信息
//            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
//            postFavourQueryWrapper.in("postId", postIdSet);
//            postFavourQueryWrapper.eq("userId", user.getId());
//            List<PostFavour> postFavourList = postFavourService.list(postFavourQueryWrapper);
//            postFavourList.forEach((postFavour -> postIdHasFavourMap.put(postFavour.getPostId(), true)));

//            //并行执行
            //获取点赞信息
            CompletableFuture<Void> getThumbFuture = CompletableFuture.runAsync(() -> {
                QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
                postThumbQueryWrapper.in("postId", postIdSet);
                postThumbQueryWrapper.eq("userId", loginUserId);
                List<PostThumb> postThumbList = postThumbService.list(postThumbQueryWrapper);
                postThumbList.forEach((postThumb -> postIdHasThumbMap.put(postThumb.getPostId(), true)));
            },paramPoolServer);
            //获取收藏信息
            CompletableFuture<Void> getFavourFuture = CompletableFuture.runAsync(() -> {
                QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
                postFavourQueryWrapper.in("postId", postIdSet);
                postFavourQueryWrapper.eq("userId", user.getId());
                List<PostFavour> postFavourList = postFavourService.list(postFavourQueryWrapper);
                postFavourList.forEach((postFavour -> postIdHasFavourMap.put(postFavour.getPostId(), true)));
            },paramPoolServer);

            try {
                CompletableFuture.allOf(getThumbFuture,getFavourFuture).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

        }
        //3.填充信息
        List<PostVO> postVOList = postList.stream().map(post -> {
            PostVO postVo = PostVO.objToVo(post);
            User userInfo = null;
            //作者信息
            if (userIdUserListMap.get(loginUserId) != null) {
                userInfo = userIdUserListMap.get(loginUserId).get(0);
            }
            postVo.setAuthor(userService.getUserVO(userInfo));
            postVo.setHasThumb(postIdHasThumbMap.getOrDefault(post.getId(), false));
            postVo.setHasFavour(postIdHasFavourMap.getOrDefault(post.getId(), false));
            return postVo;
        }).collect(Collectors.toList());
        //4.返回
        postVOPage.setRecords(postVOList);
        return postVOPage;
    }

    @Override
    public boolean deleteById(long postId, User user) {
        Post oldPost = this.getById(postId);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        //鉴权
        if(!oldPost.getUserId().equals(user.getId()) && userService.isAdmin(user)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = this.removeById(postId);
        if(result){
            //同步ES
            rabbitTemplate.convertAndSend(MqConstants.POST_EXCHANGE,MqConstants.POST_DELETE_KEY,postId);
        }
        return result;
    }
    @Override
    public int getPostThumbNum(long postId){
        Post post = this.query().select("thumbNum").eq("id", postId).one();
        if (post == null || post.getThumbNum() == null) {
            log.warn("post:{} is null!!!", postId);
            return 0;
        }
        return post.getThumbNum();
    }

    @Override
    public int getPostFavourNum(long postId){
        Post post = this.query().select("favourNum").eq("id", postId).one();
        if (post == null || post.getThumbNum() == null) {
            log.warn("post:{} is null!!!", postId);
            return 0;
        }
        return post.getFavourNum();
    }
}




