package com.yeyou.yeyoubackend.model.dto.post;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.yeyou.yeyoubackend.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * 帖子查询请求
 */
@Data
public class PostQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = -6798813948439674869L;
    /**
     * id
     */
    private Long id;

    /**
     * 不查ID
     */
    private Long notId;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 至少有一个标签
     */
    private List<String> orTags;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 收藏用户 id
     */
    private Long favourUserId;
}
