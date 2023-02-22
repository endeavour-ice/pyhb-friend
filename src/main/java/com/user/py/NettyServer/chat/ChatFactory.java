package com.user.py.NettyServer.chat;

import com.user.py.mode.constant.ChatType;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author ice
 * @Date 2022/11/12 13:17
 * @Description: 工厂+单例
 */
public class ChatFactory {
    private ChatFactory() {
    }

    private static final Map<Integer, Chat> allChat = new HashMap<Integer, Chat>() {
        private static final long serialVersionUID = -122339584041806888L;
        {
            put(ChatType.CONNECT, new ConnectChat());
            put(ChatType.FRIEND, new FriendChat());
            put(ChatType.TEAM, new TeamChat());
            put(ChatType.HEARTBEAT, new HeartbeatChat());
            put(ChatType.SYSTEM, new ChatGptChat());
        }
    };

    public static Chat getChat(Integer type) {
        return allChat.get(type);
    }
}
