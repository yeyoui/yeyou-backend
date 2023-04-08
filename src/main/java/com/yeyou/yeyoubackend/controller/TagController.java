package com.yeyou.yeyoubackend.controller;

import com.google.gson.reflect.TypeToken;
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
import java.lang.reflect.Type;
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
        //2.清除缓存
        redisCacheUtils.removeCache(RedisConstant.TAG_ALL_LIST_KEY+"ALL");
        return ResultUtils.success(success);
    }

    @GetMapping("/list")
    public BaseResponse<List<TagListDto>> list(){
        //缓存（1小时过期一次）
        Type type = new TypeToken<List<TagListDto>>(){}.getType();
        List<TagListDto> tagList = redisCacheUtils.queryWithLockNoParam(RedisConstant.TAG_ALL_LIST_KEY, "ALL",type,
                RedisConstant.TAG_ALL_LIST_LOCK + "ALL", tagService::listAll, 10, TimeUnit.SECONDS);
//        List<TagListDto> tagList=tagService.listAll();
        return ResultUtils.success(tagList);
    }

    @GetMapping("/parentList")
    public BaseResponse<List<ParentDto>> parentList(){
        //逻辑过期缓存（10秒钟过期一次）
        Type type = new TypeToken<List<ParentDto> >(){}.getType();
        List<ParentDto> tagList = redisCacheUtils.queryWithLockNoParam(RedisConstant.TAG_PARENT_LIST_KEY, "ALL",type,
                RedisConstant.TAG_PARENT_LIST_LOCK + "ALL", tagService::listAllParent, 10, TimeUnit.SECONDS);
//        List<ParentDto> tagList=tagService.listAllParent();
        return ResultUtils.success(tagList);
    }

}
