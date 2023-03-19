package com.yeyou.yeyoubackend.controller;

import com.yeyou.yeyoubackend.common.BaseResponse;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.common.ResultUtils;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.model.domain.Tag;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.dto.ParentDto;
import com.yeyou.yeyoubackend.model.dto.TagListDto;
import com.yeyou.yeyoubackend.model.request.TagAddRequest;
import com.yeyou.yeyoubackend.service.TagService;
import com.yeyou.yeyoubackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/tag")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class TagController {
    @Resource
    private UserService userService;
    @Resource
    private TagService tagService;

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
        List<TagListDto> tagList=tagService.listAll();
        return ResultUtils.success(tagList);
    }

    @GetMapping("/parentList")
    public BaseResponse<List<ParentDto>> parentList(){
        List<ParentDto> tagList=tagService.listAllParent();
        return ResultUtils.success(tagList);
    }

}
