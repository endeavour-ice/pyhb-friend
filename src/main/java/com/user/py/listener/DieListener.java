package com.user.py.listener;

import com.rabbitmq.client.Channel;
import com.user.py.mode.domain.ChatRecord;
import com.user.py.mq.AckMode;
import com.user.py.mq.MqClient;
import com.user.py.service.IChatRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author ice
 * @date 2022/8/20 18:53
 */
@Component
@Slf4j
public class DieListener {
    @Autowired
    private IChatRecordService recordService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-HH-dd hh:mm");

    @RabbitListener(queues = MqClient.DIE_QUEUE,ackMode = AckMode.MANUAL)
    public void saveDieReceiver(Message message, Channel channel, ChatRecord chatRecord)  {
        log.error("调用死信队列接收消息" + dateFormat.format(new Date()));
        try {
            boolean save = recordService.save(chatRecord);
            if (!save) {
                log.error("调用死信队列接收消息 接收失败"+dateFormat.format(new Date()));
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException ex) {
                log.error("调用死信队列接收消息 Exception接收失败"+dateFormat.format(new Date()));
            }
            log.error("调用死信队列接收消息 Exception接收失败"+dateFormat.format(new Date()));
        }

    }
}
