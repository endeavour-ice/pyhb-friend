package com.user.py.NettyServer;

import com.google.gson.Gson;
import com.user.py.NettyServer.chat.Chat;
import com.user.py.designPatten.factory.ChatFactory;
import com.user.py.designPatten.singleton.GsonUtils;
import com.user.py.mode.entity.vo.ChatRecordVo;
import com.user.py.utils.SensitiveUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ice
 * @date 2022/7/22 16:27
 */
@Slf4j
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    // 用来保存所有的客服端连接
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        // 当接收到数据后自动调用
        String message = msg.text();
        Gson gson = GsonUtils.getGson();
        Message mess;
        try {
            mess = gson.fromJson(message, Message.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            mess = GsonUtils.getGson().fromJson(message, Message.class);
        }
        ChatRecordVo chatRecord = mess.getChatRecord();
        String mes = chatRecord.getMessage();
        try {
           chatRecord.setMessage(SensitiveUtils.sensitive(mes));
        } catch (Exception ignored) {

        }
        Chat chat = ChatFactory.getChat(mess.getType());
        chat.doChat(mess, ctx);
//        switch (mess.getType()) {
//            case ChatType.CONNECT:
//                connect(mess, ctx);
//                break;
//            // 处理客服端发送消息
//            case ChatType.FRIEND:
//                privateChat(mess);
//                break;
//            case ChatType.TEAM:
//                groupChat(mess);
//                break;
//            case ChatType.HEARTBEAT:
////                log.info("接收心跳消息: "+ message);
//                break;
//        }
    }

    // 新的客服端连接时调用
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
//        log.info("创建连接{}",ctx.channel().id().asLongText());
        channels.add(ctx.channel());
    }

    // 出现异常时调用
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        log.error("出现异常,关闭连接");
        cause.printStackTrace();
        // 通道 出现异常 移除该通道
        Channel channel = ctx.channel();
        String channelId = channel.id().asLongText();
        UserChannelMap.removeByChannelId(channelId);
        channels.remove(channel);
    }

    // channel 处于活动状态调用
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        log.info("服务器地址 上线了 ~ ====> "+ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    // 用户断开连接调用
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        // 通道断开连接时 移除该通道
//        log.error("关闭连接 {}",ctx.channel().id().asLongText());
        Channel channel = ctx.channel();
        String channelId = channel.id().asLongText();
        UserChannelMap.removeByChannelId(channelId);
        channels.remove(channel);
    }

//    // 建立用户与通道的关联
//    public void connect(Message mess, ChannelHandlerContext ctx) {
//        String userId = mess.getChatRecord().getUserId();
//        UserChannelMap.put(userId, ctx.channel());
//        UserChannelMap.print();
//    }
//
//    // 私聊
//    public void privateChat(Message mess) {
//        // 将聊天消息保存到数据库
//        ChatRecordVo chatRecord = mess.getChatRecord();
//        // 发送消息好友在线,可以直接发送消息给好友
//        Channel channel = UserChannelMap.getFriendChannel(chatRecord.getSendId());
//        ChatRecord record = new ChatRecord();
//        record.setUserId(chatRecord.getUserId());
//        record.setFriendId(chatRecord.getSendId());
//        record.setMessage(chatRecord.getMessage());
//        record.setSendTime(new Date());
//        if (channel != null) {
//            record.setHasRead(1);
//            recordService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.NETTY_KEY, record);
//            channel.writeAndFlush(new TextWebSocketFrame(GsonUtils.getGson().toJson(mess)));
//        } else {
//            // 用户不在线 保存到数据库
//            record.setHasRead(0);
//            // 调用 Rabbit 保存信息
//            recordService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.NETTY_KEY, record);
//            // 不在线,暂时不发送
//        }
//    }
//
//    // 群聊
//    public void groupChat(Message mess) {
//        String teamId = mess.getChatRecord().getSendId();
//        List<String> teamUserId = team.getUserTeamListById(teamId, mess.getChatRecord().getUserId());
//        if (!CollectionUtils.isEmpty(teamUserId)) {
//            TeamChatRecord teamChatRecord = new TeamChatRecord();
//            teamChatRecord.setUserId(mess.getChatRecord().getUserId());
//            teamChatRecord.setTeamId(teamId);
//            teamChatRecord.setMessage(mess.getChatRecord().getMessage());
//            teamChatRecord.setHasRead(0);
//            teamUserId.forEach(id -> {
//                Channel teamUserChannel = UserChannelMap.getFriendChannel(id);
//                if (teamUserChannel != null) {
//                    teamUserChannel.writeAndFlush(new TextWebSocketFrame(GsonUtils.getGson().toJson(mess)));
//                }
//            });
//            recordService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.TEAM_KEY, teamChatRecord);
//
//        } else {
//            log.info("队伍人员为空: " + mess);
//        }
//    }

}
