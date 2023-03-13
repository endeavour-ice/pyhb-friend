package com.user.py.controller.PartnerController;


import com.user.py.common.B;
import com.user.py.mode.entity.TeamChatRecord;
import com.user.py.service.ITeamChatRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 队伍聊天记录表 前端控制器
 * </p>
 *
 * @author ice
 * @since 2022-09-12
 */
@RestController
@RequestMapping("/partner/teamChatRecord")
public class TeamChatRecordController {

    @Autowired
    private ITeamChatRecordService chatRecordService;


    /**
     * 根据teamId 查看队伍聊天信息
     * @param teamId
     * @param request
     * @return
     */
    @GetMapping("/getTeam")
    public B<List<TeamChatRecord>> getTeamChatRecordByTeamId(@RequestParam("teamId") String teamId, HttpServletRequest request) {
        List<TeamChatRecord>  chatRecords=chatRecordService.getTeamChatRecordByTeamId(request, teamId);
        return B.ok(chatRecords);
    }
}
