package com.user.py.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * @Author ice
 * @Date 2023/2/25 16:48
 * @Description: TODO
 */
public class RabbitMqDemo {
    private static final String QUEUE_NAME = "hello";
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        // 工厂 连接RabbitMQ
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        // 创建连接
        Connection connection = factory.newConnection();
        // 获取信道
        Channel channel = connection.createChannel();
        /**
         * 生成一个队列
         * 1. 队列名称
         * 2. 队列的消息是否是持久化(磁盘) 默认的情况下储存在内存里面
         * 3. 该队列是否是只供一个消费者进行消费,是否进行消息的共享,true可以多个消费者消费,false只有一个消费者消费
         * 4. 是否自动删除,最后一个消费者端断开连接以后,该队列是否自动删除,ture自动删除,false不自动删除
         * 5. 其他参数
         */
        channel.queueDeclare(QUEUE_NAME,false,false,true,null);
        // 发消息
        String message = "hello,world";
        /**
         * 1. 发送到哪个交换机
         * 2. 路由的key值是哪个 本次是队列的名称
         * 3. 其他参数
         * 4. 发送消息的消息体
         */
        channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
        System.out.println("消息发送成功");
        channel.basicConsume(QUEUE_NAME,true, (consumerTag, mes) -> {
            System.out.println("接收的消息"+new String(mes.getBody(), StandardCharsets.UTF_8));

        }, consumerTag -> {

        });
    }
}
