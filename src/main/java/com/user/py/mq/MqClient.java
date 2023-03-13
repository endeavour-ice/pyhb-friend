package com.user.py.mq;

/**
 * @author ice
 * @date 2022/8/20 15:49
 */

public interface MqClient {
    // 普通
    String NETTY_QUEUE = "netty_queue";
    String NETTY_KEY = "netty";
    String REDIS_QUEUE = "redis_queue";
    String DIRECT_EXCHANGE = "exchange_direct";
    String REDIS_KEY = "redis_key";
    String TEAM_QUEUE = "team_queue";
    String TEAM_KEY = "team_key";
    String READ_TEAM_QUEUE = "read_team_queue";
    String READ_TEAM_KEY = "read_team_key";
    String READ_CHAT_QUEUE = "read_chat_queue";
    String READ_CHAT_KEY = "read_chat_key";
    String OSS_QUEUE = "oss_queue";
    String OSS_KEY = "oss_key";
    // 删除 redis key
    String REMOVE_REDIS_QUEUE = "removeRedisByQueue";
    String REMOVE_REDIS_KEY = "removeRedisByKey";

    // 死信
    String DIE_EXCHANGE = "exchange_die";
    String DIE_QUEUE = "die_queue";
    String DIE_KEY = "die_key";
}
