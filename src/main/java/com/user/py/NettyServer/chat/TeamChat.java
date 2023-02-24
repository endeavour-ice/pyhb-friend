package com.user.py.NettyServer.chat;

import com.user.py.NettyServer.Message;
import com.user.py.NettyServer.UserChannelMap;
import com.user.py.mode.domain.TeamChatRecord;
import com.user.py.mq.MqClient;
import com.user.py.mq.RabbitService;
import com.user.py.service.TeamService;
import com.user.py.designPatten.singleton.GsonUtils;
import com.user.py.utils.SpringUtilObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author ice
 * @Date 2022/11/12 13:13
 * @Description: 群聊
 */
@Slf4j
public class TeamChat implements Chat {
    private static final RabbitService rabbitService = SpringUtilObject.getBean(RabbitService.class);
    private static final TeamService teamService = SpringUtilObject.getBean(TeamService.class);

    @Override
    public void doChat(Message message, ChannelHandlerContext cxt) {
        String teamId = message.getChatRecord().getSendId();
        List<String> teamUserId = teamService.getUserTeamListById(teamId, message.getChatRecord().getUserId());
        if (!CollectionUtils.isEmpty(teamUserId)) {
            TeamChatRecord teamChatRecord = new TeamChatRecord();
            teamChatRecord.setUserId(message.getChatRecord().getUserId());
            teamChatRecord.setTeamId(teamId);
            teamChatRecord.setMessage(message.getChatRecord().getMessage());
            teamChatRecord.setHasRead(0);
            teamUserId.forEach(id -> {
                Channel teamUserChannel = UserChannelMap.getChannelById(id);
                if (teamUserChannel != null) {
                    teamUserChannel.writeAndFlush(new TextWebSocketFrame(GsonUtils.getGson().toJson(message)));
                }
            });
            rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.TEAM_KEY, teamChatRecord);

        } else {
            log.info("队伍人员为空: " + message);
        }
    }
}
