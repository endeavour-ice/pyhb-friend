package com.user.py.mode.constant;

/**
 * @author ice
 * @date 2022/9/10 16:59
 */

public interface RedisKey {
    String selectFriend = "selectFriendList::";
    String tagRedisKey = "tagNum::";

    String ossAvatarUserRedisKey = "ossAvatar:User:";
    String ossAvatarTeamRedisKey = "ossAvatar:Team:";

    String redisIndexKey = "user:recommend";
    String redisAddTeamLock = "user:addTeam:key";
    String redisFileAvatarLock = "user:file:avatar:key";
    String redisFileByTeamAvatarLock = "user:file:avatar:team:lock";
    String redisFileByRegisterLock = "user:file:register:user:lock";
    String redisFileByBingDingLock = "user:file:BingDing:user:lock";
    String redisFileByBingDingKey = "user:file:BingDing:user:key";
    String redisFileByForgetLock = "user:file:forget:user:key";
    String redisRegisterCode = "redisRegisterCode:";
    String redisForgetCode = "redisRegisterCode:";

    String redisPostList = "redisPostList";


}
