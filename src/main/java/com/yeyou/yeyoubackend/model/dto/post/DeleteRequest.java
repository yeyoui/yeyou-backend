package com.yeyou.yeyoubackend.model.dto.post;

import lombok.Data;

import java.io.Serializable;

/**
 * 根据ID删除请求
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = -2436898159246221333L;
    private long id;
}
