package com.user.py.mq;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * mq 创建队列
 *
 * @author ice
 * @date 2022/8/20 11:23
 */
@Configuration
public class RabbitMqConfig {
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean("directExchange")
    public DirectExchange directExchange() {
        return new DirectExchange(MqClient.DIRECT_EXCHANGE);
    }

    @Bean("nettyQueue")
    public Queue NettyQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", MqClient.DIE_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", MqClient.DIE_KEY);
        // 设置过期时间 10 秒
        arguments.put("x-message-ttl", 10000);
        return QueueBuilder.durable(MqClient.NETTY_QUEUE).withArguments(arguments).build();
    }

    @Bean
    public Binding bindingNetty(@Qualifier("directExchange") DirectExchange directExchange,
                                @Qualifier("nettyQueue") Queue nettyQueue) {
        return BindingBuilder.bind(nettyQueue).to(directExchange).with(MqClient.NETTY_KEY);
    }

    @Bean("dieExchange")
    public DirectExchange dieExchange() {
        return new DirectExchange(MqClient.DIE_EXCHANGE);
    }

    @Bean("dieQueue")
    public Queue dieQueue() {
        return QueueBuilder.durable(MqClient.DIE_QUEUE).build();
    }

    @Bean
    public Binding dieBinding(@Qualifier("dieExchange") DirectExchange dieExchange,
                              @Qualifier("dieQueue") Queue dieQueue) {
        return BindingBuilder.bind(dieQueue).to(dieExchange).with(MqClient.DIE_KEY);
    }

    @Bean("redisQueue")
    public Queue redisQueue() {
        return QueueBuilder.durable(MqClient.REDIS_QUEUE).build();
    }

    @Bean
    public Binding redisBinding(@Qualifier("directExchange") DirectExchange dieExchange,
                                @Qualifier("redisQueue") Queue redisQueue) {
        return BindingBuilder.bind(redisQueue).to(dieExchange).with(MqClient.REDIS_KEY);
    }

    @Bean("teamQueue")
    public Queue teamQueue() {
        return QueueBuilder.durable(MqClient.TEAM_QUEUE).build();
    }

    @Bean
    public Binding teamBinding(@Qualifier("teamQueue") Queue teamQueue,
                               @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(teamQueue).to(directExchange).with(MqClient.TEAM_KEY);
    }

    @Bean("readTeamQueue")
    public Queue ReadTeamQueue() {
        return QueueBuilder.durable(MqClient.READ_TEAM_QUEUE).build();
    }

    @Bean
    public Binding ReadTeamQueueBinding(@Qualifier("readTeamQueue") Queue ReadTeamQueue,
                                        @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(ReadTeamQueue).to(directExchange).with(MqClient.READ_TEAM_KEY);
    }
    @Bean("readChatQueue")
    public Queue ReadChatQueue() {
        return QueueBuilder.durable(MqClient.READ_CHAT_QUEUE).build();
    }
    @Bean
    public Binding ReadChatQueueBinding(@Qualifier("readChatQueue") Queue readChatQueue,
                                        @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(readChatQueue).to(directExchange).with(MqClient.READ_TEAM_KEY);
    }
    @Bean("ossQueue")
    public Queue ossQueue() {
        return QueueBuilder.durable(MqClient.OSS_QUEUE).build();
    }

    @Bean
    public Binding ossQueueBinding(@Qualifier("ossQueue") Queue ossQueue,
                                   @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(ossQueue).to(directExchange).with(MqClient.OSS_KEY);
    }

    @Bean("removeRedisByKeyQueue")
    public Queue removeRedisByKeyQueue() {
        return QueueBuilder.durable(MqClient.REMOVE_REDIS_QUEUE).build();
    }

    @Bean
    public Binding removeRedisByKeyQueueBinding(@Qualifier("removeRedisByKeyQueue") Queue removeRedisByKeyQueue,
                                                @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(removeRedisByKeyQueue).to(directExchange).with(MqClient.REMOVE_REDIS_KEY);
    }
}
