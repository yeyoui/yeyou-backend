package com.yeyou.yeyoubackend.model.dto.post;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 帖子更新请求
 */
@Data
public class PostUpdateRequest implements Serializable {

    private static final long serialVersionUID = -652909893784115520L;

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 标签 json 列表(数组)
     */
    private List<String> tags;
}
