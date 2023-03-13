package com.user.py.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.entity.Team;
import com.user.py.mode.entity.User;
import com.user.py.mode.entity.vo.TeamUserAvatarVo;
import com.user.py.mode.entity.vo.TeamUserVo;
import com.user.py.mode.dto.TeamQuery;
import com.user.py.mode.request.TeamAddRequest;
import com.user.py.mode.request.TeamJoinRequest;
import com.user.py.mode.request.TeamUpdateRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author BING
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2022-08-22 15:45:11
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    String addTeam(TeamAddRequest team, User loginUser);

    /**
     * 根据id删除队伍
     * @param id
     * @param request
     * @return
     */
    boolean deleteById(String id,HttpServletRequest request);

    /**
     * 查询队伍列表
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVo> getTeamList(TeamQuery teamQuery, boolean isAdmin);


    /**
     * 添加队伍
     * @param teamJoinRequest
     * @param request
     * @return
     */
    boolean addUserTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request);

    /**
     *  修改队伍
     * @param teamUpdateRequest
     * @param request
     */
    void updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request);

    /**
     * 退出队伍
     * @param teamId 队伍的id
     * @param request  登录用户
     * @return
     */
    boolean quitTeam(String teamId, HttpServletRequest request);

    /**
     * 查看用户加入的队伍
     * @param request 1
     * @return 200
     */
    List<TeamUserAvatarVo> getJoinTeamList(HttpServletRequest request);

    /**
     * 根据id获取信息
     * @param id
     * @return
     */
    TeamUserVo getByTeamId(String id);

    boolean quitTeamByUser(String teamId, String userId, HttpServletRequest request);


     List<String> getUserTeamListById( String teamId,String userId);
    Team getTeamByTeamUser( String teamId,String userId);

    boolean updateTeamByTeam(Team team);
}
