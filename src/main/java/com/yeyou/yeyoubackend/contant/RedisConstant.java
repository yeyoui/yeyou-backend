package com.yeyou.yeyoubackend.contant;

public interface RedisConstant {
    String USER_RECOMMEND_KEY = "user:recommend:";
    String LOCK_SCHEDULE_RECOMMEND_CACHE="lock:schedule:recommendCache";
    String TEAMSECKILL_INFO_KEY="teamSeckill:stock:";
    String TEAMSECKILL_ORDERID_KEY="teamSeckill:order:";

    String TAG_PARENT_LIST_KEY="tag:parentList:";
    String TAG_ALL_LIST_KEY="tag:allList:";
    String TAG_PARENT_LIST_LOCK="Locktag:parentList:";
    String TAG_ALL_LIST_LOCK="Locktag:AllListLock:";

    String USER_ALL_USERTAGINFO_KEY="UserTag:userTag:";
    String USER_ALL_USERTAGINFO_LOCK="LockUserTag:userTag:";
    String USERVO_TEAMID_KEY="userVo:teamId:";
    String USERVO_TEAMID_LOCK="LockUserVo:teamId:";
    long CACHE_USERVO_TTL=5;
    String TEAM_INFO_KEY="team:info:";
    String TEAM_TEAMID_LOCK="LockTeam:teamId:";
    long CACHE_COMMON_TTL=5;

    String TEAMSECKILL_TEAMINFO_HASH="teamSeckill_teamHash";
    long CACHE_NULL_TTL=5;




}
