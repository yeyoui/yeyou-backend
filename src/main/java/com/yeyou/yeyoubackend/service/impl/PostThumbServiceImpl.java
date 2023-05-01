package com.yeyou.yeyoubackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyoubackend.model.domain.PostThumb;
import com.yeyou.yeyoubackend.service.PostThumbService;
import com.yeyou.yeyoubackend.mapper.PostThumbMapper;
import org.springframework.stereotype.Service;

/**
* @author lhy
* @description 针对表【post_thumb(帖子点赞表)】的数据库操作Service实现
* @createDate 2023-04-27 15:39:43
*/
@Service
public class PostThumbServiceImpl extends ServiceImpl<PostThumbMapper, PostThumb>
    implements PostThumbService{

}




