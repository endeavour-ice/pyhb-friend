package com.user.py.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.entity.UserTeam;
import com.user.py.mode.entity.vo.UserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author BING
* @description 针对表【user_team(队伍表)】的数据库操作Service
* @createDate 2022-08-22 15:55:33
*/
public interface UserTeamService extends IService<UserTeam> {

    List<UserVo> getUserTeamById(String teamId, HttpServletRequest request);
}
