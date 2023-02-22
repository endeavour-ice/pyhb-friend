package com.user.py.NettyServer.chat;

import com.user.py.NettyServer.Message;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author ice
 * @Date 2022/11/12 13:23
 * @Description: 心跳
 */
public class HeartbeatChat implements Chat {
    @Override
    public void doChat(Message message, ChannelHandlerContext cxt) {

    }
}
