package com.yeyou.yeyoubackend.controller;

import com.yeyou.yeyoubackend.common.BaseResponse;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.common.ResultUtils;
import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.dto.ParentDto;
import com.yeyou.yeyoubackend.model.dto.TagListDto;
import com.yeyou.yeyoubackend.model.request.TagAddRequest;
import com.yeyou.yeyoubackend.service.TagService;
import com.yeyou.yeyoubackend.service.UserService;
import com.yeyou.yeyoubackend.utils.StringRedisCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/tag")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class TagController {
    @Resource
    private UserService userService;
    @Resource
    private TagService tagService;
    @Resource
    private StringRedisCacheUtils redisCacheUtils;

    @PostMapping("/add")
    public BaseResponse<Long> addTag(@RequestBody TagAddRequest tagAddRequest, HttpServletRequest request){
        //1.校验参数信息
        if(tagAddRequest==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long success = tagService.addTag(tagAddRequest, loginUser);
        return ResultUtils.success(success);
    }

    @GetMapping("/list")
    public BaseResponse<List<TagListDto>> list(){
        //逻辑过期缓存（1分钟更新一次）
//        List<TagListDto> tagList = redisCacheUtils.queryWithLogicalExpireNoParam(RedisConstant.TAG_ALL_LIST_KEY, "ALL",
//                RedisConstant.TAG_ALL_LIST_LOCK + "ALL", tagService::listAll, 1, TimeUnit.MINUTES);
        List<TagListDto> tagList=tagService.listAll();
        return ResultUtils.success(tagList);
    }

    @GetMapping("/parentList")
    public BaseResponse<List<ParentDto>> parentList(){
        //逻辑过期缓存(1小时更新一次
//        List<ParentDto> tagList = redisCacheUtils.queryWithLogicalExpireNoParam(RedisConstant.TAG_PARENT_LIST_KEY, "ALL",
//                RedisConstant.TAG_PARENT_LIST_LOCK + "ALL", tagService::listAllParent, 1, TimeUnit.HOURS);
        List<ParentDto> tagList=tagService.listAllParent();
        return ResultUtils.success(tagList);
    }

}
