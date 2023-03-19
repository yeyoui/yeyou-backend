package com.yeyou.yeyoubackend.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class TagAddRequest implements Serializable {

    private static final long serialVersionUID = -1392135640734690406L;
    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 父标签 id
     */
    private Long parentId;

    /**
     * 0 - 不是, 1 - 父标签
     */
    private Integer isParent;
}
