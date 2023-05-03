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
import com.yeyou.yeyoubackend.service.PostThumbService;
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
@RequestMapping("/postThumb")
@Slf4j
public class PostThumbController {
    @Resource
    private PostThumbService postThumbService;

    /**
     * 点赞操作
     * @param idRequest
     * @return
     */
    @PostMapping("/")
    public BaseResponse<Integer> doPostThumb(@RequestBody IdRequest idRequest){
        if(idRequest==null || idRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long postId = idRequest.getId();
        int result = postThumbService.doPostThumb(postId, UserHold.get());
        return ResultUtils.success(result);
    }

}
