package com.user.py.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.domain.ChatRecord;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
     * @param userId 用户id
     * @param friendId 朋友id
     * @param request
     * @return 集合
     */
    List<ChatRecord> selectAllList(String userId, String friendId, HttpServletRequest request);
}
