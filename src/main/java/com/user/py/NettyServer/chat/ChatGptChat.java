package com.user.py.NettyServer.chat;

import com.user.py.NettyServer.Message;
import com.user.py.common.ErrorCode;
import com.user.py.designPatten.singleton.DataUtils;
import com.user.py.designPatten.singleton.GsonUtils;
import com.user.py.exception.GlobalException;
import com.user.py.mode.entity.ChatRecord;
import com.user.py.mode.entity.vo.ChatRecordVo;
import com.user.py.mq.MqClient;
import com.user.py.mq.RabbitService;
import com.user.py.utils.ChatGptUtils;
import com.user.py.utils.SpringUtilObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * @Author ice
 * @Date 2023/2/12 16:40
 * @Description: TODO
 */
@Slf4j
public class ChatGptChat implements Chat {
    private static final RabbitService rabbitService = SpringUtilObject.getBean(RabbitService.class);

    @Override
    public void doChat(Message message, ChannelHandlerContext cxt) {
        ChatRecordVo chatRecord = message.getChatRecord();
        String mess = chatRecord.getMessage();
        if (!StringUtils.hasText(mess)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请输入消息");
        }
        ChatRecord record = new ChatRecord();
        String userId = chatRecord.getUserId();
        record.setUserId(userId);
        String sendId = chatRecord.getSendId();
        record.setFriendId(sendId);
        String recordMessage = chatRecord.getMessage();
        record.setMessage(recordMessage);
        record.setSendTime(DataUtils.getFdt().format(new Date()));
        String toMess;

        rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.NETTY_KEY, record);
        toMess = ChatGptUtils.sendChatGpt(chatRecord.getUserId(),mess);
        record.setMessage(toMess);
        record.setUserId(sendId);
        record.setFriendId(userId);
        record.setSendTime(DataUtils.getFdt().format(new Date()));
        chatRecord.setMessage(toMess);
        cxt.channel().writeAndFlush(new TextWebSocketFrame(GsonUtils.getGson().toJson(message)));
        rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.NETTY_KEY, record);
    }
}
