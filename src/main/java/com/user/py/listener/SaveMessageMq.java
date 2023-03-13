package com.user.py.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.user.py.mapper.MessageMqMapper;
import com.user.py.mode.entity.MessageMq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * @Author ice
 * @Date 2023/2/24 9:24
 * @Description: TODO
 */
@Component
@Slf4j
public class SaveMessageMq {
    @Resource
    private MessageMqMapper messageMqMapper;

    public boolean saveMessage(Message message) {
       return save(message, null);
    }
    public boolean saveMessage(Message message,String error) {
        return save(message, error);
    }
    private boolean save(Message message,String error) {
        String messageId = message.getMessageProperties().getMessageId();
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            if (messageId == null || !StringUtils.hasText(body)) {
                return false;
            }
            MessageMq messageMq = new MessageMq();
            messageMq.setMessageId(messageId);
            messageMq.setMessageBody(body);
            messageMq.setError(error);
            QueryWrapper<MessageMq> wrapper = new QueryWrapper<>();
            wrapper.eq("message_id", messageId);
            Long count = messageMqMapper.selectCount(wrapper);
            if (count != 0) {
                return false;
            }
            return messageMqMapper.insert(messageMq)>0;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }
    public void saveMessage(String id,String error) {
        MessageMq messageMq = messageMqMapper.selectById(id);
        if (messageMq != null) {
            messageMq.setError(error);
            messageMqMapper.updateById(messageMq);
        }
    }
}
