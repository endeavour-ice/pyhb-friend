package com.user.py.NettyServer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ice
 * @date 2022/7/27 9:29
 */
@Slf4j
public class HearBeatHandler extends ChannelInboundHandlerAdapter {
    //  * 捕获的 添加Netty空闲超时检查
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // 判断类型
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent)evt;

            if(idleStateEvent.state() == IdleState.READER_IDLE) {
//                log.info("读空闲事件触发...");
            }
            else if(idleStateEvent.state() == IdleState.WRITER_IDLE) {
//                log.info("写空闲事件触发...");
            }
            else if(idleStateEvent.state() == IdleState.ALL_IDLE) {
//                log.info("读写空闲事件触发...");
                Channel channel = ctx.channel();
                String channelId = channel.id().asLongText();
                UserChannelMap.removeByChannelId(channelId);
                channel.close();
//                log.info("关闭通道 {} 资源",channelId);
            }
        }
    }
}
