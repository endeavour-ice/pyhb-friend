package com.user.py.listener;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.user.py.designPatten.singleton.GsonUtils;
import com.user.py.mapper.ChatRecordMapper;
import com.user.py.mq.AckMode;
import com.user.py.mq.MqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/3/3 18:18
 * @Description: TODO
 */
@Component
@Slf4j
public class ChatListener {
    @Resource
    private ChatRecordMapper chatRecordMapper;
    @Resource
    private SaveMessageMq messageMq;

    @RabbitListener(queues = MqClient.READ_CHAT_QUEUE, ackMode = AckMode.MANUAL)
    public void chatRecord(Message message, Channel channel) {
        boolean saveMessage = messageMq.saveMessage(message);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Gson gson = GsonUtils.getGson();
        String mes = new String(message.getBody(), StandardCharsets.UTF_8);
        if (saveMessage) {
            List<String> ids = gson.fromJson(mes, new TypeToken<List<String>>() {
            }.getType());

            if (!ids.isEmpty()) {
                chatRecordMapper.updateReadBatchById(ids);
            }else {
                messageMq.saveMessage(message, "要处理的数据为空");
            }
        }
        try {

            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }
}
