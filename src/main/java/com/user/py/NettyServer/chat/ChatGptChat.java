package com.user.py.NettyServer.chat;

import com.user.py.NettyServer.Message;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mode.domain.ChatRecord;
import com.user.py.mode.domain.vo.ChatRecordVo;
import com.user.py.mq.MqClient;
import com.user.py.mq.RabbitService;
import com.user.py.utils.ChatGptUtils;
import com.user.py.utils.GsonUtils;
import com.user.py.utils.SpringUtilObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * @Author ice
 * @Date 2023/2/12 16:40
 * @Description: TODO
 */
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
        record.setUserId(chatRecord.getUserId());
        record.setFriendId(chatRecord.getSendId());
        record.setMessage(chatRecord.getMessage());
        record.setSendTime(new Date());
        String toMess;
        try {
            toMess = ChatGptUtils.sendChatG(mess);
        } catch (Exception e) {
            toMess = "系统异常，请稍后!";
        }
        chatRecord.setMessage(toMess);
        rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.NETTY_KEY, record);
        record.setMessage(toMess);
        rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.NETTY_KEY, record);
        cxt.channel().writeAndFlush(new TextWebSocketFrame(GsonUtils.getGson().toJson(message)));
    }
}
