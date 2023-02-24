package com.user.py.controller.PartnerController;


import com.user.py.common.B;
import com.user.py.mode.domain.vo.UserVo;
import com.user.py.service.UserTeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author ice
 * @date 2022/9/13 9:51
 */
@RestController
@RequestMapping("/partner/userTeam")
public class UserTeamController {

    @Autowired
    private UserTeamService userTeamService;

    /**
     * 根据队伍id获取队员的信息
     * @param teamId
     * @param request
     * @return
     */
    @GetMapping("/get")
    public B<List<UserVo>> getUserTeamById(@RequestParam("teamId") String teamId, HttpServletRequest request) {
        List<UserVo> users = userTeamService.getUserTeamById(teamId, request);
        return B.ok(users);
    }

}
