package com.yeyou.yeyoucommon.constant;

public interface MqConstants {
    /**
     * Post交换机
     */
    String POST_EXCHANGE = "post.topic";
    /**
     * 监听新增和修改的队列
     */
    String POST_INSERT_QUEUE = "post.insert.queue";
    /**
     * 监听删除的队列
     */
    String POST_DELETE_QUEUE = "post.delete.queue";
    /**
     * 新增或修改的RoutingKey
     */
    String POST_INSERT_KEY = "post.insert";
    /**
     * 删除的RoutingKey
     */
    String POST_DELETE_KEY = "post.delete";

}
