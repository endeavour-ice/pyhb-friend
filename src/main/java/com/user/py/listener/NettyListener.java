package com.user.py.listener;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.user.py.designPatten.singleton.GsonUtils;
import com.user.py.mode.entity.ChatRecord;
import com.user.py.mode.entity.TeamChatRecord;
import com.user.py.mq.AckMode;
import com.user.py.mq.MqClient;
import com.user.py.service.IChatRecordService;
import com.user.py.service.ITeamChatRecordService;
import com.user.py.utils.SensitiveUtils;
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
 * @date 2022/8/20 16:08
 */
@Component
@Slf4j
public class NettyListener {
    @Autowired
    private IChatRecordService chatRecordService;
    @Autowired
    private ITeamChatRecordService teamChatRecordService;

    @Resource
    private SaveMessageMq saveMessageMq;

    @RabbitListener(queues = MqClient.NETTY_QUEUE, ackMode = AckMode.MANUAL)
    public void SaveChatRecord(Message message, Channel channel) {
        MessageProperties messageProperties = message.getMessageProperties();
        String messageId = messageProperties.getMessageId();
        long deliveryTag = messageProperties.getDeliveryTag();
        try {
            Gson gson = GsonUtils.getGson();
            ChatRecord chatRecord = gson.fromJson(new String(message.getBody(), StandardCharsets.UTF_8), ChatRecord.class);
            if (chatRecord != null) {
                String mes = chatRecord.getMessage();
                try {
                    mes= SensitiveUtils.sensitive(mes);
                } catch (Exception e) {
                    log.error("过滤失败！！");
                }

                chatRecord.setMessage(mes);
                boolean save = chatRecordService.save(chatRecord);
                if (!save) {
                    saveMessageMq.saveMessage(message, "保存聊天记录失败");
                    log.error("保存聊天记录失败");
                }
            } else {
                saveMessageMq.saveMessage(message, "chatRecord == null ");
                log.error("保存聊天记录失败");
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            saveMessageMq.saveMessage(messageId, e.getMessage());
            log.error("保存聊天记录失败" + e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("消息队列拒绝失败" + e.getMessage());
            }
        }


    }

    @RabbitListener(queues = MqClient.TEAM_QUEUE)
    public void SaveTeamChatRecord(Message message, Channel channel, TeamChatRecord chatRecord) {
        MessageProperties messageProperties = message.getMessageProperties();
        String messageId = messageProperties.getMessageId();
        long deliveryTag = messageProperties.getDeliveryTag();

        if (chatRecord != null) {
            boolean save = teamChatRecordService.save(chatRecord);
            if (!save) {
                log.error("保存队伍聊天记录失败...");
            }
        } else {
            log.error("保存队伍聊天记录失败...");
        }

        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("消息队列拒绝失败" + e.getMessage());
        }
    }

}
