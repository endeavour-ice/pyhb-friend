package com.user.py.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.user.py.mode.entity.Team;
import com.user.py.mode.entity.vo.TeamUserAvatarVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @author BING
 * @description 针对表【team(队伍表)】的数据库操作Mapper
 * @createDate 2022-08-22 15:45:11
 * @Entity com.user.usercenter.model.domain.Team
 */
public interface TeamMapper extends BaseMapper<Team> {

    List<TeamUserAvatarVo> selectJoinTeamUserList(@Param("userId") String userId);
}




