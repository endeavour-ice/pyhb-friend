package com.user.py.listener;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.user.py.mode.domain.ChatRecord;
import com.user.py.mode.domain.TeamChatRecord;
import com.user.py.mq.AckMode;
import com.user.py.mq.MqClient;
import com.user.py.service.IChatRecordService;
import com.user.py.service.ITeamChatRecordService;
import com.user.py.designPatten.singleton.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
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
        String messageId = message.getMessageProperties().getMessageId();
        boolean saveMessage = saveMessageMq.saveMessage(message);
        if (saveMessage) {
            try {
                Gson gson = GsonUtils.getGson();
                ChatRecord chatRecord = gson.fromJson(new String(message.getBody(), StandardCharsets.UTF_8), ChatRecord.class);
                if (chatRecord != null) {
                    boolean save = chatRecordService.save(chatRecord);
                    if (!save) {
                        saveMessageMq.saveMessage(messageId,"保存聊天记录失败");
                        log.error("保存聊天记录失败");
                    }
                } else {
                    log.error("保存聊天记录失败");
                }
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception e) {
                saveMessageMq.saveMessage(messageId,e.getMessage());
                log.error("保存聊天记录失败" + e.getMessage());
                try {
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                } catch (IOException ex) {
                    log.error("消息队列拒绝失败" + e.getMessage());
                }
            }
        } else {

            log.error("消息重复消费，消息ID: " + messageId);
        }


    }

    @RabbitListener(queues = MqClient.TEAM_QUEUE)
    public void SaveTeamChatRecord(Message message, Channel channel, TeamChatRecord chatRecord) {
        boolean saveMessage = saveMessageMq.saveMessage(message);
        if (saveMessage) {
            if (chatRecord != null) {
                boolean save = teamChatRecordService.save(chatRecord);
                if (!save) {
                    log.error("保存队伍聊天记录失败...");
                }
            } else {
                log.error("保存队伍聊天记录失败...");
            }
        } else {
            log.error("消息重复消费，消息ID: " + message.getMessageProperties().getMessageId());
        }

    }

}
