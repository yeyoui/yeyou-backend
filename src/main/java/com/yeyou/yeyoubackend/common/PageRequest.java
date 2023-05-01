package com.yeyou.yeyoubackend.common;

import com.yeyou.yeyoubackend.contant.CommonConstant;
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
    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;
}
