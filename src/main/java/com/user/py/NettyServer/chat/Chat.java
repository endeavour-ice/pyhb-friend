package com.user.py.NettyServer.chat;

import com.user.py.NettyServer.Message;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author ice
 * @Date 2022/11/12 13:10
 * @Description: TODO
 */
public interface Chat {
    /**
     * 聊天类型
     *
     * @param message 前端发送的信息体
     * @param cxt
     */
    void doChat(Message message, ChannelHandlerContext cxt);
}
