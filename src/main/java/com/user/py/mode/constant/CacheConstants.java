package com.user.py.mode.constant;

/**
 * @author ice
 * @date 2022/9/10 16:59
 */

public interface CacheConstants {
    String selectFriend = "selectFriendList::";
    String TAG_REDIS_KEY = "tagNum::";

    String OSS_AVATAR_USER_REDIS_KEY = "ossAvatar:User:";
    String OSS_AVATAR_TEAM_REDIS_KEY = "ossAvatar:Team:";

    String REDIS_INDEX_KEY = "user:recommend";
    String REDIS_ADD_TEAM_LOCK = "user:addTeam:key";
    String REDIS_FILE_AVATAR_LOCK = "user:file:avatar:key";
    String REDIS_FILE_BY_TEAM_AVATAR_LOCK = "user:file:avatar:team:lock";
    String REDIS_FILE_BY_REGISTER_LOCK = "user:file:register:user:lock";
    String REDIS_FILE_BY_BING_DING_LOCK = "user:file:BingDing:user:lock";
    String REDIS_FILE_BY_BING_DING_KEY = "user:file:BingDing:user:key";
    String REDIS_FILE_BY_FORGET_LOCK = "user:file:forget:user:key";
    String REDIS_REGISTER_CODE = "redisRegisterCode:";
    String REDIS_FORGET_CODE = "redisRegisterCode:";

    String REDIS_POST_LIST = "redisPostList";
    String USER_TOTAL = "userTotal";

    String POST_TOTAL = "postTotal";
    String CAPTCHA_CODE_KEY = "image_code";
    String IS_LOGIN = "user:is:login";
}
