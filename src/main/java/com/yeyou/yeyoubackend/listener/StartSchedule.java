package com.yeyou.yeyoubackend.listener;

import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.model.dto.ParentDto;
import com.yeyou.yeyoubackend.model.dto.TagListDto;
import com.yeyou.yeyoubackend.service.TagService;
import com.yeyou.yeyoubackend.utils.StringRedisCacheUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class StartSchedule implements ApplicationListener<org.springframework.context.event.ContextRefreshedEvent> {

    @Resource
    private TagService tagService;
    @Resource
    private StringRedisCacheUtils redisCacheUtils;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //初始化缓存
        //初始化父标签列表（逻辑过期）
        List<ParentDto> parentDtoList = tagService.listAllParent();
        redisCacheUtils.setWithLogicalExpire(RedisConstant.TAG_PARENT_LIST_KEY+"ALL",parentDtoList,1, TimeUnit.HOURS);
        //初始化所有标签（逻辑过期）
        List<TagListDto> tagList = tagService.listAll();
        redisCacheUtils.setWithLogicalExpire(RedisConstant.TAG_ALL_LIST_KEY+"ALL",tagList,1, TimeUnit.MINUTES);
    }
}
