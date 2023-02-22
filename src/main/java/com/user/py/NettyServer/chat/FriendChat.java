package com.user.py.NettyServer.chat;

import com.user.py.NettyServer.Message;
import com.user.py.NettyServer.UserChannelMap;
import com.user.py.mode.domain.ChatRecord;
import com.user.py.mode.domain.vo.ChatRecordVo;
import com.user.py.mq.MqClient;
import com.user.py.mq.RabbitService;
import com.user.py.utils.GsonUtils;
import com.user.py.utils.SpringUtilObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Date;

/**
 * @Author ice
 * @Date 2022/11/12 13:12
 * @Description: 好友聊天
 */
public class FriendChat implements Chat{


    private static final RabbitService rabbitService = SpringUtilObject.getBean(RabbitService.class);
    @Override
    public void doChat(Message mess, ChannelHandlerContext cxt) {
        // 将聊天消息保存到数据库
        ChatRecordVo chatRecord = mess.getChatRecord();
        // 发送消息好友在线,可以直接发送消息给好友
        Channel channel = UserChannelMap.getChannelById(chatRecord.getSendId());
        ChatRecord record = new ChatRecord();
        record.setUserId(chatRecord.getUserId());
        record.setFriendId(chatRecord.getSendId());
        record.setMessage(chatRecord.getMessage());
        record.setSendTime(new Date());
        if (channel != null) {
            record.setHasRead(1);
            rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.NETTY_KEY, record);
            channel.writeAndFlush(new TextWebSocketFrame(GsonUtils.getGson().toJson(mess)));
        } else {
            // 用户不在线 保存到数据库
            record.setHasRead(0);
            // 调用 Rabbit 保存信息
            rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.NETTY_KEY, record);
            // 不在线,暂时不发送
        }
    }
}
