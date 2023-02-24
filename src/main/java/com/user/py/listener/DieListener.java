package com.user.py.listener;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.user.py.designPatten.singleton.GsonUtils;
import com.user.py.mode.domain.ChatRecord;
import com.user.py.mq.AckMode;
import com.user.py.mq.MqClient;
import com.user.py.service.IChatRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
    @Resource
    private SaveMessageMq messageMq;

    @RabbitListener(queues = MqClient.DIE_QUEUE,ackMode = AckMode.MANUAL)
    public void saveDieReceiver(Message message, Channel channel)  {
        boolean saveMessage = messageMq.saveMessage(message);
        String messageId = message.getMessageProperties().getMessageId();
        if (saveMessage) {
            log.info("调用死信队列接收消息 ID" +messageId);
            try {
                Gson gson = GsonUtils.getGson();
                ChatRecord chatRecord = gson.fromJson(new String(message.getBody(), StandardCharsets.UTF_8), ChatRecord.class);
                boolean save = recordService.save(chatRecord);
                if (!save) {
                    messageMq.saveMessage(messageId,"调用死信队列接收消息 接收失败");
                    log.error("调用死信队列接收消息 接收失败，消息ID"+ messageId);
                }
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (Exception e) {
                try {
                    messageMq.saveMessage(messageId,e.getMessage());
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
                } catch (IOException ex) {
                    log.error("调用死信队列接收消息 Exception接收失败,消息ID"+ messageId);
                }
                log.error("调用死信队列接收消息 Exception接收失败ID"+ messageId);
            }
        } else {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
            } catch (IOException e) {
                log.error("消息拒绝失败: "+e.getMessage());
            }
            log.error("消息重复消费，消息ID: "+ messageId+" 消费消息: "+new String(message.getBody(),StandardCharsets.UTF_8));
        }


    }
}
