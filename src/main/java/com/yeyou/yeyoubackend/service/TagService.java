package com.yeyou.yeyoubackend.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.yeyou.yeyoubackend.model.domain.Tag;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.dto.ParentDto;
import com.yeyou.yeyoubackend.model.dto.TagListDto;
import com.yeyou.yeyoubackend.model.request.TagAddRequest;

import java.util.List;

/**
* @author lhy
* @description 针对表【tag(标签)】的数据库操作Service
* @createDate 2023-03-13 14:00:02
*/
public interface TagService extends IService<Tag> {

    /**
     * 新增标签
     * @param tagAddRequest
     * @param loginUser
     * @return 插入的ID
     */
    Long addTag(TagAddRequest tagAddRequest, User loginUser);

    /**
     * 返回所有标签
     * @return
     */
    List<TagListDto> listAll();
    /**
     * 返回父标签
     * @return
     */
    List<ParentDto> listAllParent();
}
