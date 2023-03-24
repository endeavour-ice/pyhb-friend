package com.user.py.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mapper.ChatRecordMapper;
import com.user.py.mode.entity.ChatRecord;
import com.user.py.mode.entity.User;
import com.user.py.mode.entity.vo.ChatList;
import com.user.py.mode.entity.vo.ChatVo;
import com.user.py.mq.MqClient;
import com.user.py.mq.RabbitService;
import com.user.py.service.IChatRecordService;
import com.user.py.service.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 聊天记录表 服务实现类
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
@Service
public class ChatRecordServiceImpl extends ServiceImpl<ChatRecordMapper, ChatRecord> implements IChatRecordService {
    @Resource
    private RabbitService rabbitService;
    @Resource
    private IUserService userService;

    @Override
    public ChatList selectAllList(String friendId, String userId) {
        if (StringUtils.hasText(userId) && StringUtils.hasText(friendId)) {
            User user = userService.getById(friendId);
            if (user == null) {
                throw new GlobalException(ErrorCode.NULL_ERROR, "好友不存在");
            }
            int count = baseMapper.selectUserAddFriend(userId, friendId);
            if (count <= 0) {
                return null;
            }
            ChatList chatList = new ChatList();
            List<ChatRecord> records = baseMapper.selectAllByUserIdAndFriendId(userId, friendId);
            if (!records.isEmpty()) {
                List<String> ids = records.stream().map(ChatRecord::getId).collect(Collectors.toList());
                List<ChatVo> chatVoList = records.stream().map(chatRecord -> {
                    ChatVo chatVo = new ChatVo();
                    chatVo.setId(chatRecord.getId());
                    chatVo.setUserId(chatRecord.getUserId());
                    chatVo.setFriendId(chatRecord.getFriendId());
                    chatVo.setMessage(chatRecord.getMessage());
                    chatVo.setSendTime(chatRecord.getSendTime());
                    return chatVo;
                }).sorted(Comparator.comparing(ChatVo::getSendTime)).collect(Collectors.toList());
                chatList.setChat(chatVoList);
                rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE,MqClient.READ_CHAT_KEY,ids);
            }
            chatList.setFriendId(user.getId());
            chatList.setName(user.getUserAccount());
            chatList.setAvatarUrl(user.getAvatarUrl());
            return chatList;
        }
        return null;
    }
}
