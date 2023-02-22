package com.user.py.mq;

/**
 * @author ice
 * @date 2022/8/20 15:49
 */

public class MqClient {
    // 普通
    public static final String DIRECT_EXCHANGE = "exchange_direct";
    public static final String NETTY_QUEUE = "netty_queue";
    public static final String NETTY_KEY = "netty";
    public static final String REDIS_QUEUE = "redis_queue";
    public static final String REDIS_KEY = "redis_key";
    public static final String TEAM_QUEUE = "team_queue";
    public static final String TEAM_KEY = "team_key";
    public static final String READ_TEAM_QUEUE = "read_team_queue";
    public static final String READ_TEAM_KEY = "read_team_key";
    public static final String OSS_QUEUE = "oss_queue";
    public static final String OSS_KEY = "oss_key";
    // 删除 redis key
    public static final String REMOVE_REDIS_QUEUE = "removeRedisByQueue";
    public static final String REMOVE_REDIS_KEY = "removeRedisByKey";

    // 死信
    public static final String DIE_EXCHANGE ="exchange_die";
    public static final String DIE_QUEUE = "die_queue";
    public static final String DIE_KEY ="die_key";
}
