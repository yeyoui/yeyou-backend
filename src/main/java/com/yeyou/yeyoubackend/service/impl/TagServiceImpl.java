package com.yeyou.yeyoubackend.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.mapper.TagMapper;
import com.yeyou.yeyoubackend.model.domain.Tag;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.dto.ParentDto;
import com.yeyou.yeyoubackend.model.dto.TagChildren;
import com.yeyou.yeyoubackend.model.dto.TagListDto;
import com.yeyou.yeyoubackend.model.request.TagAddRequest;
import com.yeyou.yeyoubackend.service.TagService;
import com.yeyou.yeyoubackend.utils.StringRedisCacheUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author lhy
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2023-03-13 14:00:02
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService{
    @Override
    public Long addTag(TagAddRequest tagAddRequest, User loginUser) {
        //1. 校验参数信息
        if(tagAddRequest==null || StringUtils.isEmpty(tagAddRequest.getTagName()))
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //2. 是否是父标签
        Integer isParent = tagAddRequest.getIsParent();
        if(isParent!=null && isParent==1){
            tagAddRequest.setParentId(null);
            //1.检查父标签请求
            //2. 检查是否有存在的标签
            long count = this.query().eq("tagName", tagAddRequest.getTagName()).eq("isParent", 1).count();
            if(count!=0) throw new BusinessException(ErrorCode.PARAMS_ERROR,"标签已存在");
        }else{
            tagAddRequest.setIsParent(0);
            //2.检查子标签请求
            //2.1父标签是否存在
            Long parentId = tagAddRequest.getParentId();
            if(parentId==null || parentId<0 || this.query().eq("id",parentId).eq("isParent",1).count()==0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            //2.2 检查是否有存在的标签
            long count = this.query().eq("tagName", tagAddRequest.getTagName()).eq("parentId", parentId).count();
            if(count!=0) throw new BusinessException(ErrorCode.PARAMS_ERROR,"标签已存在");
        }
        //4. 新增
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagAddRequest,tag);
        tag.setUserId(loginUser.getId());
        boolean result = this.save(tag);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return tag.getId();
    }

    @Override
    public List<TagListDto> listAll() {
        //1. 查询父标签列表
        List<Tag> parentList = this.query().eq("isParent", 1).list();
        ArrayList<TagListDto> tagListDtos = new ArrayList<>(parentList.size());
        //2. 通过父标签填充子标签
        for (Tag parentTag : parentList) {
            //封装父标签
            TagListDto tagListEntry = new TagListDto();
            tagListEntry.setText(parentTag.getTagName());
            tagListEntry.setId(parentTag.getId());
            //封装当前父标签所有子标签
            List<Tag> children = this.query().eq("parentId", parentTag.getId()).list();

            List<TagChildren> childrenList = children.stream().map((child) -> {
                TagChildren tagChildren = new TagChildren();
                tagChildren.setText(child.getTagName());
                tagChildren.setId(child.getTagName());
                return tagChildren;
            }).collect(Collectors.toList());

            tagListEntry.setChildren(childrenList);

            tagListDtos.add(tagListEntry);
        }
        return tagListDtos;
    }

    @Override
    public List<ParentDto> listAllParent() {
        //1. 查询父标签列表
        List<Tag> parentList = this.query().eq("isParent", 1).list();
        List<ParentDto> collect = parentList.stream().map((parent -> {
            ParentDto tagListDto = new ParentDto();
            tagListDto.setText(parent.getTagName());
            tagListDto.setValue(parent.getId());
            return tagListDto;
        })).collect(Collectors.toList());
        collect.add(new ParentDto("父标签",0L));
        return collect;
    }
}




