package com.user.py.NettyServer.chat;

import com.user.py.NettyServer.Message;
import com.user.py.NettyServer.UserChannelMap;
import com.user.py.designPatten.singleton.DataUtils;
import com.user.py.mode.entity.ChatRecord;
import com.user.py.mode.entity.vo.ChatRecordVo;
import com.user.py.mq.MqClient;
import com.user.py.mq.RabbitService;
import com.user.py.designPatten.singleton.GsonUtils;
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
        record.setSendTime(DataUtils.getFdt().format(new Date()));
        record.setHasRead(0);
        if (channel != null) {
            channel.writeAndFlush(new TextWebSocketFrame(GsonUtils.getGson().toJson(mess)));
        }
        rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.NETTY_KEY, record);
    }
}
