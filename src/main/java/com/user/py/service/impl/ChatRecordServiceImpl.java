package com.user.py.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.mapper.ChatRecordMapper;
import com.user.py.mode.entity.ChatRecord;
import com.user.py.mq.MqClient;
import com.user.py.mq.RabbitService;
import com.user.py.service.IChatRecordService;
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

    @Override
    public List<ChatRecord> selectAllList(String userId, String friendId) {
        if (StringUtils.hasText(userId) && StringUtils.hasText(friendId)) {
            int count = baseMapper.selectUserAddFriend(userId, friendId);
            if (count <= 0) {
                return null;
            }
            List<ChatRecord> records = baseMapper.selectAllByUserIdAndFriendId(userId, friendId);
            if (!records.isEmpty()) {
                List<String> ids = records.stream().map(ChatRecord::getId).collect(Collectors.toList());
                records= records.stream()
                        .sorted(Comparator.comparing(ChatRecord::getSendTime)).collect(Collectors.toList());
                rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE,MqClient.READ_CHAT_KEY,ids);
            }
            return records;
        }
        return null;
    }
}
