package com.user.py.controller.PartnerController;


import com.user.py.common.B;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mode.domain.Team;
import com.user.py.mode.domain.User;
import com.user.py.mode.domain.vo.TeamUserVo;
import com.user.py.mode.dto.TeamQuery;
import com.user.py.mode.request.TeamAddRequest;
import com.user.py.mode.request.TeamJoinRequest;
import com.user.py.mode.request.TeamUpdateRequest;
import com.user.py.mode.request.UserTeamQuitRequest;
import com.user.py.service.TeamService;
import com.user.py.utils.UserUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author ice
 * @date 2022/8/22 16:02
 */
@RestController
@RequestMapping("/partner/team")
public class TeamController {

    @Autowired
    private TeamService teamService;

    /**
     * 创建队伍
     * @param teamAddRequest
     * @param request
     * @return
     */
    @PostMapping("addTeam")
    public B<String> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }

        User loginUser = UserUtils.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        String userID = teamService.addTeam(team, loginUser);
        if (!StringUtils.hasText(userID)) {
            return B.error(ErrorCode.PARAMS_ERROR,"队伍保存失败");
        }
        return B.ok(userID);
    }

    /**
     * 删除队伍
     * @param teamId
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public B<Boolean> deleteTeamById(@RequestBody(required = false) String teamId,HttpServletRequest request) {
        if (!StringUtils.hasText(teamId)) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        boolean b = teamService.deleteById(teamId,request);
        if (!b) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除失败");
        }
        return B.ok();
    }

    /**
     * 跟新 队伍
     * @param request
     * @return
     */
    @PostMapping("update")
    public B<Boolean> update(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {

        teamService.updateTeam(teamUpdateRequest,request);
        return B.ok();
    }

    @GetMapping("/get")
    public B<TeamUserVo> getTeamById(@RequestParam("id") String id) {
        if (!StringUtils.hasText(id)) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        TeamUserVo team = teamService.getByTeamId(id);
        if (team == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        return B.ok(team);
    }

    /**
     * 根据条件查询所有的队伍
     * @param teamQuery 条件
     * @param request is
     * @return 200
     */
    @GetMapping("/list")
    public B<List<TeamUserVo>> getTeamList(TeamQuery teamQuery, HttpServletRequest request) {
        boolean admin = UserUtils.isAdmin(request);
        List<TeamUserVo> resultPage = teamService.getTeamList(teamQuery,admin);
        return B.ok(resultPage);
    }

    /**
     * 查看用户加入的队伍
     * @param request 1
     * @return 200
     */
    @GetMapping("/Check")
    public B<List<TeamUserVo>> getJoinTeamList(HttpServletRequest request) {
        List<TeamUserVo> list=teamService.getJoinTeamList(request);
        return B.ok(list);
    }

    /**
     *  加入队伍
     * @param teamJoinRequest 队伍参数
     * @param request 登录
     * @return b
     */
    @PostMapping ("/join")
    public B<Boolean> addUserTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        boolean add = teamService.addUserTeam(teamJoinRequest, request);
        if (add) {
            return B.ok();
        }
        return B.error(ErrorCode.ERROR);
    }

    /**
     * 退出队伍
     * @param teamId 队伍id
     * @param request 响应
     * @return 返回
     */
    @GetMapping("/quit")
    public B<Boolean> quitTeam(@RequestParam String teamId, HttpServletRequest request) {
        boolean isQuit = teamService.quitTeam(teamId, request);
        if (!isQuit) {
            return B.error(ErrorCode.ERROR,"退出错误,请重试");
        }
        return B.ok();
    }

    /**
     * 队伍队长 退队员
     * @param request 队伍id
     * @return b
     */
    @PostMapping("/quitUserTeam")
    public B<Boolean> quitTeamByUser(@RequestBody UserTeamQuitRequest userTeamQuitRequest, HttpServletRequest request) {
        if (userTeamQuitRequest == null){
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        String teamId = userTeamQuitRequest.getTeamId();
        String userId = userTeamQuitRequest.getUserId();
        boolean isQuit = teamService.quitTeamByUser(teamId, userId, request);
        if (isQuit) {
            return B.ok();
        }
        return B.error(ErrorCode.PARAMS_ERROR,"删除失败");
    }
}
