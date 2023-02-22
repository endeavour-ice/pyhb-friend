package com.user.py.mq;

import com.google.gson.Gson;
import com.user.py.mode.domain.ChatRecord;
import com.user.py.service.IChatRecordService;
import com.user.py.utils.EmailUtil;
import com.user.py.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * @Author ice
 * @Date 2022/10/22 21:02
 * @PackageName:com.user.py.mq
 * @ClassName: MyCallBack
 * @Description: TODO
 * @Version 1.0
 */
@Component
@Slf4j
public class MyCallBack implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {
    @Autowired
    private IChatRecordService recordService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    private void init() {
        // 交换机触发回调接口
        rabbitTemplate.setConfirmCallback(this);
        // 队列触发回调接口
        rabbitTemplate.setReturnsCallback(this);
    }

    /**
     * 交换机确认回调的方法
     *
     * @param correlationData 保存回调的消息
     * @param ack             交换机是否收到消息
     * @param cause           失败的原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (!ack) {
            ReturnedMessage message = correlationData.getReturned();
            ChatRecord chatRecord = null;
            if (message != null) {
                try {
                    Gson gson = GsonUtils.getGson();
                    boolean save;
                    int a = 0;
                    String messages = new String(message.getMessage().getBody(), StandardCharsets.UTF_8);
                    chatRecord = gson.fromJson(messages, ChatRecord.class);
                    do {
                        save = recordService.save(chatRecord);
                        a++;
                    } while (!save && a <= 5);
                    boolean isEmail = EmailUtil.sendAlarmEmail("聊天信息保存失败,失败原因: " + cause + " 失败信息为: " + chatRecord);
                    if (!isEmail) {
                        log.error("报警短信发送失败");
                    }
                } catch (Exception e) {
                    EmailUtil.sendAlarmEmail("聊天信息保存失败,失败原因: " + cause + " 失败信息为: " + chatRecord + "报错原因为: " + e.getMessage());
                }
            }

        }
    }

    // 可以当消息传递的过程中不可达目的地时将消息返回给生产者
    // 只有传不到目的地时才会回退
    @Override
    public void returnedMessage(@NotNull ReturnedMessage returnedMessage) {

    }
}
