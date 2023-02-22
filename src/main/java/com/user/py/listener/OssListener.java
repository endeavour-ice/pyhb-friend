package com.user.py.listener;

import com.rabbitmq.client.Channel;
import com.user.py.mode.domain.User;
import com.user.py.mq.MqClient;
import com.user.py.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author ice
 * @date 2022/9/17 13:34
 */
@Component
@Slf4j
public class OssListener {
    @Autowired
    private IUserService userService;

    @RabbitListener(queues = MqClient.OSS_QUEUE)
    public void SaveUserUrl(Message message, Channel channel, User user) {
        try {
            if (user != null) {
                boolean save = userService.updateById(user);
                if (!save) {
                    log.error("保存用户头像失败...");
                }
            }else {
                log.error("保存用户头像失败....");

            }
        } catch (Exception e) {
            log.error("保存用户头像失败....." + e.getMessage());
        }
    }
}
