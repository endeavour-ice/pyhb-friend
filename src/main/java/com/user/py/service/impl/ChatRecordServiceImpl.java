package com.user.py.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mapper.ChatRecordMapper;
import com.user.py.mode.domain.ChatRecord;
import com.user.py.service.IChatRecordService;
import com.user.py.utils.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    private TransactionTemplate transactionTemplate;

    @Override
    public List<ChatRecord> selectAllList(String userId, String friendId, HttpServletRequest request) {
        return transactionTemplate.execute(status -> {
            try {
                UserUtils.getLoginUser(request);
                QueryWrapper<ChatRecord> wrapper = new QueryWrapper<>();
                wrapper.eq("user_id", userId).and(q -> {
                    q.eq("friend_id", friendId);
                }).or().eq("user_id", friendId).and(q -> {
                    q.eq("friend_id", userId);
                });
                ChatRecord chatRecord = new ChatRecord();
                chatRecord.setHasRead(1);
                baseMapper.update(chatRecord, wrapper);
                List<ChatRecord> chatRecords = baseMapper.selectList(wrapper);
                chatRecords = chatRecords.parallelStream().sorted(Comparator.comparing(ChatRecord::getSendTime)).collect(Collectors.toList());
                if (chatRecords.size() <= 0) {
                    return null;
                }
                return chatRecords;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
            }
        });
    }
}
