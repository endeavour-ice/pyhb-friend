package com.user.py.NettyServer.chat;

import com.user.py.NettyServer.Message;
import com.user.py.NettyServer.UserChannelMap;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author ice
 * @Date 2022/11/12 13:15
 * @Description: 建立连接
 */
public class ConnectChat implements Chat{
    @Override
    public void doChat(Message message, ChannelHandlerContext cxt) {
        String userId = message.getChatRecord().getUserId();
        UserChannelMap.put(userId, cxt.channel());
        UserChannelMap.print();
    }
}
