package com.user.py.NettyServer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author ice
 * @date 2022/7/22 21:13
 */
@Component
@Slf4j
public class NettyListenerStart implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private WebSocketNettyServer webSocketNettyServer;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            try {
                webSocketNettyServer.start();
            } catch (Exception e) {
                log.error("Netty启动失败 原因: "+e.getMessage());
            }
        }
    }
}
