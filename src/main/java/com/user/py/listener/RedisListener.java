package com.user.py.listener;

import com.rabbitmq.client.Channel;
import com.user.py.mq.MqClient;
import com.user.py.utils.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @Author ice
 * @Date 2022/9/28 9:42
 * @PackageName:com.user.usercenter.listener
 * @ClassName: RedisListener
 * @Description: 监听Redis的操作
 * @Version 1.0
 */
@Component
@Slf4j
public class RedisListener {

    @Resource
    private RedisCache redisCache;
    @Resource
    private SaveMessageMq saveMessageMq;

    @RabbitListener(queues = MqClient.REMOVE_REDIS_QUEUE)
    public void removeRedisByKey(Message message, Channel channel, String redisKey) {
        MessageProperties messageProperties = message.getMessageProperties();
        long deliveryTag = messageProperties.getDeliveryTag();
        if (StringUtils.hasText(redisKey)) {
            if (redisCache.hasKey(redisKey)) {
                boolean delete = redisCache.deleteObject(redisKey);
                if (delete) {
                    log.info("删除redis =>  key: {} 成功", redisKey);
                } else {
                    log.error("删除redis =>  key: {} 失败", redisKey);
                }
            }
        } else {
            log.error("删除redis的key为空");
        }
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("接受失败: " + e.getMessage());
            saveMessageMq.saveMessage(message, e.getMessage());
        }
    }
}
