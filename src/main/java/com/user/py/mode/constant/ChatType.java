package com.user.py.mode.constant;

/**
 * 聊天的类型
 * @author ice
 * @date 2022/9/12 19:08
 */

public interface ChatType {
    int SYSTEM = 4;

    /**
     * 连接
     */
    int CONNECT = 0;

    /**
     * 好友
     */
    int FRIEND = 1;
    /**
     * 队伍
     */
    int TEAM = 2;

    /**
     * 心跳
     */
    int HEARTBEAT = 3;

}
