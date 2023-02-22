package com.user.py.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author ice
 * @date 2022/8/20 16:03
 */
@Service
public class RabbitService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public  void  sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
    }
}
