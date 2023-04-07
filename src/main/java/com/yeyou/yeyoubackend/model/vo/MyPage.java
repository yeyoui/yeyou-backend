package com.yeyou.yeyoubackend.model.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class MyPage<T> extends Page<T> {
    public MyPage(long current, long size) {
        super(current,size);
    }

    @Override
    public String toString() {
        return current+":"+size;
    }
}
