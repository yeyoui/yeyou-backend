package com.yeyou.yeyoubackend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeyou.yeyoucommon.model.domain.Post;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class PostFavourPostByPage {
    @Resource
    private PostFavourMapper postFavourMapper;

    @Test
    void listUserFavourPstByPage(){
        IPage<Post> iPage=new Page<>(1, 1);
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.eq("id", 1);
        postQueryWrapper.like("content", "摸鱼");
        Page<Post> postPage = postFavourMapper.listFavourPostByPage(iPage, postQueryWrapper, 1L);
        System.out.println(postPage.getRecords());
    }
}
