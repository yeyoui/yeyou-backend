package com.yeyou.yeyoubackend.contant;

public interface RedisConstant {
    String USER_MATCH_KEY = "user:match:";
    String USER_MATCH_LOCK = "user:match:lock:";
    long USER_MATCH_TTL = 60;

    String USER_RECOMMEND_KEY = "user:recommend:";
    String USER_RECOMMEND_LOCK = "user:recommend:lock:";
    long USER_RECOMMEND_TTL = 1;
    String LOCK_SCHEDULE_MATCH_CACHE ="lock:schedule:matchCache";
    String TEAMSECKILL_INFO_KEY="teamSeckill:stock:";
    String TEAMSECKILL_ORDERID_KEY="teamSeckill:order:";

    String TAG_PARENT_LIST_KEY="tag:parentList:";
    String TAG_ALL_LIST_KEY="tag:allList:";
    String TAG_PARENT_LIST_LOCK="Locktag:parentList:";
    String TAG_ALL_LIST_LOCK="Locktag:AllListLock:";

    String USER_ALLUSERINFO_KEY ="userInfo:allUser:all";
    String USER_USERINFO_ONLY_UID_TAGS_LOCK="LockUserInfo:allUser:";
    String USER_ALL_USERTAGINFO_KEY="UserTag:userTag:";
    long USER_ALL_USERTAGINFO_TTL=1;
    String USER_ALL_USERTAGINFO_LOCK="LockUserTag:userTag:";
    String USERVO_TEAMID_KEY="userVo:teamId:";
    String USERVO_TEAMID_LOCK="LockUserVo:teamId:";
    long CACHE_USERVO_TTL=5;
    String TEAM_INFO_KEY="team:info:";
    String TEAM_TEAMID_LOCK="LockTeam:teamId:";
    long CACHE_COMMON_TTL=5;

    String TEAMSECKILL_TEAMINFO_HASH="teamSeckill_teamHash";
    String USER_TOKEN_KEY = "user_token_key:";
    long CACHE_NULL_TTL=5;


}
