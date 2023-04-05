package com.yeyou.yeyoubackend.contant;

public interface RedisConstant {
    String  USER_RECOMMEND_KEY = "user:recommend:";
    String LOCK_SCHEDULE_RECOMMEND_CACHE="lock:schedule:recommendCache";
    String TEAMSECKILL_INFO_KEY="teamSeckill:stock:";
    String TEAMSECKILL_ORDERID_KEY="teamSeckill:order:";
    String TEAM_INFO_KEY="team:info:";
    String TAG_PARENT_LIST_KEY="tag:parentList:";
    String TAG_ALL_LIST_KEY="tag:allList:";
    String TAG_PARENT_LIST_LOCK="tag:parentListLock:";
    String TAG_ALL_LIST_LOCK="tag:AllListLock:";
    long CACHE_NULL_TTL=5;


}
