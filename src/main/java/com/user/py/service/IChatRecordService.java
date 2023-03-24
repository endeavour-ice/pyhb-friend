package com.user.py.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.entity.ChatRecord;
import com.user.py.mode.entity.vo.ChatList;

/**
 * <p>
 * 聊天记录表 服务类
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
public interface IChatRecordService extends IService<ChatRecord> {


    /**
     *  查询所有的聊天记录
     * @param friendId 用户id
     * @param friendId 朋友id
     * @return 集合
     */
    ChatList selectAllList(String userId, String friendId);
}
