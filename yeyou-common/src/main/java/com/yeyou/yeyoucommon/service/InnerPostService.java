package com.yeyou.yeyoucommon.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeyou.yeyoucommon.model.domain.Post;
import com.yeyou.yeyoucommon.model.vo.PostVO;

import java.util.List;

public interface InnerPostService {
    /**
     * 封装帖子信息
     * @param postPage
     * @param userToken
     * @return
     */
    Page<PostVO> getPostVOPage(Page<Post> postPage, String userToken);

    /**
     * 根据帖子ID列表获取帖子信息
     * @param postIdList
     * @return
     */
    List<Post> selectBatchIds(List<Long> postIdList);

    Post getPostById(long id);
}
