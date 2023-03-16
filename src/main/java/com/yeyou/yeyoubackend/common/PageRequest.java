package com.yeyou.yeyoubackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用页面请求参数
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -7477642174339103141L;
    /**
     * 页面大小
     */
    protected int pageSize=10;
    /**
     * 当前页面
     */
    protected int pageNum=1;
}
