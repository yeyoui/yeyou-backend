package com.yeyou.yeyoubackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeyou.yeyoubackend.common.BaseResponse;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.common.ResultUtils;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.exception.ThrowUtils;
import com.yeyou.yeyoubackend.model.domain.Post;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.dto.IdRequest;
import com.yeyou.yeyoubackend.model.dto.post.PostQueryRequest;
import com.yeyou.yeyoubackend.model.vo.PostVO;
import com.yeyou.yeyoubackend.service.PostFavourService;
import com.yeyou.yeyoubackend.service.PostService;
import com.yeyou.yeyoubackend.utils.UserHold;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 帖子收藏接口
 */
@RestController
@RequestMapping("/postFavour")
@Slf4j
public class PostFavourController {
    @Resource
    private PostFavourService postFavourService;

    @Resource
    private PostService postService;

    /**
     * 收藏操作
     * @param idRequest
     * @return
     */
    @PostMapping("/")
    public BaseResponse<Integer> doPostFavour(@RequestBody IdRequest idRequest){
        if(idRequest==null || idRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long postId = idRequest.getId();
        int result = postFavourService.doPostFavour(postId, UserHold.get());
        return ResultUtils.success(result);
    }

    /**
     * 获取已收藏的帖子列表
     * @param postQueryRequest
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<PostVO>> listMyFavourPostListByPage(@RequestBody PostQueryRequest postQueryRequest){
        //参数为空
        ThrowUtils.throwIf(postQueryRequest==null,ErrorCode.PARAMS_ERROR);
        //获取登录用户
        User loginUser = UserHold.get();
        int pageNum = postQueryRequest.getPageNum();
        int pageSize = postQueryRequest.getPageSize();
        //限制爬虫
        ThrowUtils.throwIf(pageSize>20,ErrorCode.PARAMS_ERROR);
        Page<Post> postPage = postFavourService.listFavourListByPage(new Page<>(pageNum, pageSize),
                postService.getQueryWrapper(postQueryRequest), loginUser.getId());
        return ResultUtils.success(postService.getPostVoPage(postPage, loginUser));
    }

    @PostMapping("/list/page")
    public BaseResponse<Page<PostVO>> listFavourPostListByPage(@RequestBody PostQueryRequest postQueryRequest){
        //参数为空
        ThrowUtils.throwIf(postQueryRequest==null,ErrorCode.PARAMS_ERROR);
        //获取登录用户
        User loginUser = UserHold.get();
        int pageNum = postQueryRequest.getPageNum();
        int pageSize = postQueryRequest.getPageSize();
        ThrowUtils.throwIf(postQueryRequest.getUserId()==null,ErrorCode.PARAMS_ERROR);
        Long userId = postQueryRequest.getUserId();
        //限制爬虫
        ThrowUtils.throwIf(pageSize>20,ErrorCode.PARAMS_ERROR);
        Page<Post> postPage = postFavourService.listFavourListByPage(new Page<>(pageNum, pageSize),
                postService.getQueryWrapper(postQueryRequest), userId);
        return ResultUtils.success(postService.getPostVoPage(postPage, loginUser));
    }

}
