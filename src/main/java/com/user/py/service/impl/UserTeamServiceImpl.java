package com.user.py.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mapper.UserTeamMapper;
import com.user.py.mode.domain.User;
import com.user.py.mode.domain.UserTeam;
import com.user.py.mode.domain.vo.UserVo;
import com.user.py.service.IUserService;
import com.user.py.service.UserTeamService;
import com.user.py.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author BING
* @description 针对表【user_team(队伍表)】的数据库操作Service实现
* @createDate 2022-08-22 15:55:33
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {
    @Autowired
    private IUserService userService;

    @Override
    public List<UserVo> getUserTeamById(String teamId, HttpServletRequest request) {
        if (!StringUtils.hasText(teamId)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        UserUtils.getLoginUser(request);
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("team_id", teamId);
        List<UserTeam> userTeams = this.list(wrapper);
        if (userTeams == null || userTeams.size() <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        List<String> userIdList = new ArrayList<>();
        userTeams.forEach(userTeam -> {
            String userId = userTeam.getUserId();
            userIdList.add(userId);
        });
        List<User> users = userService.listByIds(userIdList);
        if (users == null || users.size() <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return users.stream().map(UserUtils::getSafetyUser).collect(Collectors.toList());

    }
}




