package com.yeyou.yeyoubackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yeyou.yeyoubackend.common.BaseResponse;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.common.ResultUtils;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.exception.ThrowUtils;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.dto.post.DeleteRequest;
import com.yeyou.yeyoubackend.model.dto.post.PostAddRequest;
import com.yeyou.yeyoubackend.model.dto.post.PostQueryRequest;
import com.yeyou.yeyoubackend.model.dto.post.PostUpdateRequest;
import com.yeyou.yeyoubackend.service.PostService;
import com.yeyou.yeyoubackend.service.UserService;
import com.yeyou.yeyoubackend.utils.UserHold;
import com.yeyou.yeyoucommon.model.domain.Post;
import com.yeyou.yeyoucommon.model.vo.PostVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {

    private Gson GSON=new Gson();
    @Resource
    private PostService postService;
    @Resource
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<Long> addPost(@RequestBody PostAddRequest postAddRequest){
        //1.校验参数
        if(postAddRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //转换
        Post post = new Post();
        BeanUtils.copyProperties(postAddRequest,post);
        List<String> tags=postAddRequest.getTags();
        if(!CollectionUtils.isEmpty(tags)){
            post.setTags(GSON.toJson(tags));
        }
        //2.将请求交给Service处理
        //获取用户信息
        User user = UserHold.get();
        long postId = postService.savePost(post,user);
        //3.返回数据
        return ResultUtils.success(postId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest){
        //1.校验参数
        if(deleteRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long postId = deleteRequest.getId();
        User user = UserHold.get();
        boolean result = postService.deleteById(postId,user);
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updatePost(@RequestBody PostUpdateRequest postUpdateRequest){
        //1.校验参数
        if(postUpdateRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //转换
        Post post = new Post();
        BeanUtils.copyProperties(postUpdateRequest,post);
        List<String> tags=postUpdateRequest.getTags();
        if(!CollectionUtils.isEmpty(tags)){
            post.setTags(GSON.toJson(tags));
        }
        //2.将请求交给Service处理
        //获取用户信息
        User user = UserHold.get();
        boolean success = postService.updatePost(post,user);
        //3.返回数据
        return ResultUtils.success(success);
    }

    /**
     * 根据 id 获取
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PostVO> getPostVOById(long id) {
        ThrowUtils.throwIf(id <= 0,ErrorCode.PARAMS_ERROR);
        Post post = postService.getById(id);
        ThrowUtils.throwIf(post == null,ErrorCode.NOT_FOUND_ERROR);
        User user = UserHold.get();
        return ResultUtils.success(postService.getPostVO(post,user));
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PostVO>> listPostVoByPage(@RequestBody PostQueryRequest postQueryRequest){
        ThrowUtils.throwIf(postQueryRequest==null,ErrorCode.PARAMS_ERROR);
        // 限制爬虫
        int pageSize = postQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize>20,ErrorCode.PARAMS_ERROR);
        User user = UserHold.get();
        Page<PostVO> postPage = postService.listPostVoPage(postQueryRequest,user);
        return ResultUtils.success(postPage);
    }

    @PostMapping("/list/my/page/vo")
    public BaseResponse<Page<PostVO>> listMyPostVoByPage(@RequestBody PostQueryRequest postQueryRequest){
        ThrowUtils.throwIf(postQueryRequest==null,ErrorCode.PARAMS_ERROR);
        // 限制爬虫
        long current = postQueryRequest.getPageNum();
        int pageSize = postQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize>20,ErrorCode.PARAMS_ERROR);
        //获取用户信息
        User loginUser = UserHold.get();
        long uid = Optional.of(loginUser.getId()).orElse(-1L);
        ThrowUtils.throwIf(uid==-1,ErrorCode.PARAMS_ERROR,"获取用户信息失败");
        //设置要查询的用户
        postQueryRequest.setUserId(uid);
        //查询
        Page<Post> postPage = postService.page(new Page<>(current, pageSize), postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(postService.getPostVoPage(postPage, loginUser));
    }


}
