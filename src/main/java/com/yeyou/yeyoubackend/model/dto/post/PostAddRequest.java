package com.yeyou.yeyoubackend.model.dto.post;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子新增请求
 */
@Data
public class PostAddRequest implements Serializable {
    private static final long serialVersionUID = 8076869389964689566L;
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
