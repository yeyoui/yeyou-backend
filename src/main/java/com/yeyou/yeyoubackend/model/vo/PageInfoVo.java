package com.yeyou.yeyoubackend.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageInfoVo implements Serializable {

    private static final long serialVersionUID = 4579102024929875446L;
    private long total;
    private long curPage;
}
