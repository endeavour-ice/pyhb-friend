package com.user.py.listener;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.user.py.mode.domain.User;
import com.user.py.mq.MqClient;
import com.user.py.service.IUserService;
import com.user.py.designPatten.singleton.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ice
 * @date 2022/9/17 13:34
 */
@Component
@Slf4j
public class OssListener {
    @Autowired
    private IUserService userService;
    @Resource
    private SaveMessageMq saveMessageMq;
    @RabbitListener(queues = MqClient.OSS_QUEUE)
    public void SaveUserUrl(Message message, Channel channel) {
        boolean saveMessage = saveMessageMq.saveMessage(message);
        MessageProperties messageProperties = message.getMessageProperties();
        long deliveryTag = messageProperties.getDeliveryTag();
        String messageId = messageProperties.getMessageId();
        if (saveMessage) {
            try {
                Gson gson = GsonUtils.getGson();
                User user = gson.fromJson(new String(message.getBody(), StandardCharsets.UTF_8), User.class);
                if (user != null) {
                    boolean save = userService.updateById(user);
                    if (!save) {
                        saveMessageMq.saveMessage(messageId,"保存用户头像失败");
                        log.error("保存用户头像失败ID: "+messageId);
                    }
                } else {
                    log.error("用户头像为空: ID"+messageId);

                }
                channel.basicAck(deliveryTag,false);
            } catch (Exception e) {
                saveMessageMq.saveMessage(messageId,e.getMessage());
                log.error("保存用户头像失败: ID " +messageId+ e.getMessage());
            }
        } else {
            try {
                channel.basicAck(deliveryTag,false);
            } catch (IOException ignored) {
                log.error("消息接受失败: "+message);
            }
            log.error("消息重复消费，消息ID: "+messageId);
        }

    }
}
