package com.yeyou.yeyoubackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyoubackend.model.domain.PostFavour;
import com.yeyou.yeyoubackend.service.PostFavourService;
import com.yeyou.yeyoubackend.mapper.PostFavourMapper;
import org.springframework.stereotype.Service;

/**
* @author lhy
* @description 针对表【post_favour(帖子收藏表)】的数据库操作Service实现
* @createDate 2023-04-27 15:39:43
*/
@Service
public class PostFavourServiceImpl extends ServiceImpl<PostFavourMapper, PostFavour>
    implements PostFavourService{

}




