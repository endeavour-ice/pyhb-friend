package com.user.py.service.impl;

import com.user.py.mapper.TeamMapper;
import com.user.py.mode.entity.vo.TeamUserAvatarVo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;


@SpringBootTest
class TeamServiceImplTest {
    @Resource
    private TeamMapper teamMapper;
    @Test
    public void test() {
        List<TeamUserAvatarVo> teamUserAvatarVos = teamMapper.selectJoinTeamUserList("1574011394461388801");
        teamUserAvatarVos.forEach(System.out::println);

    }
}