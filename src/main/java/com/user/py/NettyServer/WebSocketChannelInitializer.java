package com.user.py.NettyServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

/**
 * @author ice
 * @date 2022/7/22 16:09
 *
 * 通道初始化器
 * 用来加载通道处理器
 */
@Component
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    // 初始化管道
    // 加载对应的ChannelHandler
    @Override
    protected void initChannel(SocketChannel ch){
        ChannelPipeline pipeline = ch.pipeline();
        // 添加一个http 的编码器
        pipeline.addLast(new HttpServerCodec());
        // 添加一个用于大数据支持的支持
        pipeline.addLast(new ChunkedWriteHandler());
        // 添加一个聚合去,这个聚合器主要是将httpMessage 聚合成 FullHttpRequest/Response
        pipeline.addLast(new HttpObjectAggregator(1024 * 64));
        // 需要指定接收请求的路由
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        // 添加Netty空闲超时检查 当触发后会调用下一个handler
        // 1. 读空闲超时(超过一定事件会发送对应的事件)
        // 2. 写空闲超时
        // 3. 读写空闲超时
        pipeline.addLast(new IdleStateHandler(12, 15, 20));
        // 对空闲检测的handler
        pipeline.addLast(new HearBeatHandler());
        // 添加自定义的handler
        pipeline.addLast(new ChatHandler());
    }
}
