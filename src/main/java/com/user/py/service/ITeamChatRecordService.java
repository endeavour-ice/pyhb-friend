package com.user.py.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.entity.TeamChatRecord;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 队伍聊天记录表 服务类
 * </p>
 *
 * @author ice
 * @since 2022-09-12
 */
public interface ITeamChatRecordService extends IService<TeamChatRecord> {

    /**
     * 根据team id 删除队伍的聊天信息
     *
     * @param teamId
     * @return
     */
    boolean deleteTeamChatRecordByTeamId(String teamId);

    List<TeamChatRecord> getTeamChatRecordByTeamId(HttpServletRequest request, String teamId);
}
