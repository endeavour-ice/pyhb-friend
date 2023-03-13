package com.user.py.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.common.ErrorCode;
import com.user.py.designPatten.singleton.DataUtils;
import com.user.py.exception.GlobalException;
import com.user.py.mapper.TeamMapper;
import com.user.py.mode.constant.RedisKey;
import com.user.py.mode.dto.TeamQuery;
import com.user.py.mode.entity.Team;
import com.user.py.mode.entity.User;
import com.user.py.mode.entity.UserTeam;
import com.user.py.mode.entity.vo.TeamUserAvatarVo;
import com.user.py.mode.entity.vo.TeamUserVo;
import com.user.py.mode.entity.vo.UserVo;
import com.user.py.mode.enums.TeamStatusEnum;
import com.user.py.mode.request.TeamAddRequest;
import com.user.py.mode.request.TeamJoinRequest;
import com.user.py.mode.request.TeamUpdateRequest;
import com.user.py.service.ITeamChatRecordService;
import com.user.py.service.IUserService;
import com.user.py.service.TeamService;
import com.user.py.service.UserTeamService;
import com.user.py.utils.MD5;
import com.user.py.utils.UserUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author BING
 * @description 针对表【team(队伍表)】的数据库操作Service实现
 * @createDate 2022-08-22 15:45:11
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {
    @Autowired
    private RedissonClient redissonClient;

    @Resource
    private UserTeamService userTeamService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private IUserService userService;
    @Autowired
    private ITeamChatRecordService chatRecordService;

    // 时间格式器

    @Override
    public TeamUserVo getByTeamId(String id) {
        Team team = this.getById(id);
        if (team == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("team_id", id);
        List<UserTeam> userTeams = userTeamService.list(userTeamQueryWrapper);
        List<String> ids = new ArrayList<>();
        userTeams.forEach(userTeam -> {
            String userId = userTeam.getUserId();
            ids.add(userId);
        });
        List<User> list = userService.listByIds(ids);
        ArrayList<UserVo> userVos = new ArrayList<>();
        list.forEach(user -> {
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            userVos.add(userVo);
        });
        TeamUserVo teamUserVo = new TeamUserVo();
        BeanUtils.copyProperties(team, teamUserVo);
        teamUserVo.setUserVo(userVos);
        return teamUserVo;
    }

    @Override
    public String addTeam(TeamAddRequest team, User loginUser) {
        RLock lock = redissonClient.getLock(RedisKey.redisAddTeamLock);
        try {
            if (lock.tryLock(0, 3000, TimeUnit.MILLISECONDS)) {
                Team tm = new Team();
                tm.setId(tm.getId());
                tm.setName(team.getName());
                tm.setDescription(team.getDescription());
                tm.setMaxNum(team.getMaxNum());
                tm.setPassword(team.getPassword());
                tm.setStatus(team.getStatus());
                String expireTime = team.getExpireTime();

                if (StringUtils.hasText(expireTime)) {
                    SimpleDateFormat fdt = DataUtils.getSf();
                    Date date = fdt.parse(expireTime);
                    tm.setExpireTime(date);
                }
                return getAddTeam(tm, loginUser);
            }
            return "";
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "加锁失败");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    public String getAddTeam(Team team, User loginUser) {
        //        校验信息
        //队伍人数 > 1 且 <= 20
        long maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0L);
        if (maxNum < 1 || maxNum >= 20) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //队伍标题 <= 20
        String teamName = team.getName();
        if (!StringUtils.hasText(teamName) && teamName.length() >= 20) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        String teamDescription = team.getDescription();
        //描述 <= 512
        if (teamDescription.length() >= 512) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍描述不满足要求");
        }
        //status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        String password = team.getPassword();
        TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusByValue(status);
        //如果 status 是加密状态，一定要有密码，且密码 <= 32
        if (TeamStatusEnum.ENCRYPTION.equals(statusEnum) && (!StringUtils.hasText(password) || password.length() > 32)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍密码不满足要求");
        }
        //超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime == null || new Date().after(expireTime)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        //校验用户最多创建 5 个队伍
        String userId = loginUser.getId();
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        long count = this.count(wrapper);
        if (count > 5) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户最多创建5个队伍");
        }
        //        插入队伍信息到队伍表
        return transactionTemplate.execute(sta -> {
            team.setId(null);
            team.setUserId(userId);
            team.setAvatarUrl("https://pic1.zhimg.com/80/v2-88c46f9f5b2aa6d6e04469fb989b7b54_720w.jpg");
            int insert = baseMapper.insert(team);
            String teamId = team.getId();
            if (insert != 1 || !StringUtils.hasText(teamId)) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "创建失败");
            }
            //插入用户 => 队伍关系到关系表
            UserTeam userTeam = new UserTeam();
            userTeam.setName(team.getName());
            userTeam.setUserId(userId);
            userTeam.setTeamId(teamId);
            userTeam.setJoinTime(new Date());
            boolean result = userTeamService.save(userTeam);
            if (!result) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "创建失败");
            }
            return teamId;
        });

    }

    /**
     * 根据id删除队伍
     *
     * @param id      队伍的id
     * @param request 登录用户
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(String id, HttpServletRequest request) {
        User loginUser = UserUtils.getLoginUser(request);
        String userId = loginUser.getId();
        if (!StringUtils.hasText(userId)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "未登录");
        }
        Team team = this.getById(id);
        if (team == null || !StringUtils.hasText(team.getId())) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "没有该队伍");
        }
        String teamId = team.getId();
        String teamUserId = team.getUserId();
        if (!userId.equals(teamUserId) || !UserUtils.isAdmin(request)) {
            throw new GlobalException(ErrorCode.NO_AUTH, "没有权限");
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("team_id", teamId);
        boolean remove = userTeamService.remove(userTeamQueryWrapper);
        if (!remove) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除队伍关联失败");
        }
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("user_id", userId).and(wrapper -> wrapper.eq("id", teamId));
        boolean b = this.remove(teamQueryWrapper);
        if (b) {
            boolean record = chatRecordService.deleteTeamChatRecordByTeamId(teamId);
            if (!record) {
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除失败...");
            }
            return true;
        }
        return false;
    }

    @Override
    public List<TeamUserVo> getTeamList(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        wrapper.and(wr -> wr.gt(true, "expire_time", new Date()).or().isNotNull("expire_time"));
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                wrapper.eq("id", id);
            }
            String searchTxt = teamQuery.getSearchTxt();
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                wrapper.eq("max_num", maxNum);
            }
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                wrapper.eq("user_id", userId);
            }
            if (StringUtils.hasText(searchTxt)) {
                wrapper.like("name", searchTxt).or().like("description", searchTxt);
            }
            Integer status = teamQuery.getStatus();
            TeamStatusEnum teamStatusByValue = TeamStatusEnum.getTeamStatusByValue(status);

            if (teamStatusByValue == null) {
                wrapper.and(wr -> wr.eq("status", TeamStatusEnum.PUBLIC.getValue())
                        .or().eq("status", TeamStatusEnum.ENCRYPTION.getValue()));
            }
            if (teamStatusByValue != null && isAdmin && teamStatusByValue.equals(TeamStatusEnum.PRIVATE)) {
                wrapper.and(wr -> wr.eq("status", TeamStatusEnum.PUBLIC.getValue()).
                        or().eq("status", TeamStatusEnum.ENCRYPTION.getValue()).
                        or().eq("status", TeamStatusEnum.PRIVATE.getValue()));
            }
        }


        /*
        select *
               from team t
         left join user_team ut on t.id = ut.team_id
         left join user u on t.user_id = u.id
         */
        List<Team> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<TeamUserVo> teamUserVos = new ArrayList<>();
        for (Team team : list) {
            String teamId = team.getId();
            QueryWrapper<UserTeam> teamQueryWrapper = new QueryWrapper<>();
            teamQueryWrapper.eq("team_id", teamId);
            List<UserTeam> userTeams = userTeamService.list(teamQueryWrapper);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            if (!CollectionUtils.isEmpty(userTeams)) {
                for (UserTeam userTeam : userTeams) {
                    String userId = userTeam.getUserId();
                    User userById = userService.getById(userId);
                    if (userById != null) {
                        UserVo userVo = new UserVo();
                        BeanUtils.copyProperties(userById, userVo);
                        teamUserVo.getUserVo().add(userVo);
                    }
                }
            }
            teamUserVos.add(teamUserVo);
//            Long userId = team.getUserId();
//            if (userId == null) {
//                continue;
//            }
//            User user = userService.getUserById(userId.toString());
//
//            User safetyUser = UserUtils.getSafetyUser(user);
//            // 返回脱敏的用户消息
//            TeamUserVo teamUserVo = new TeamUserVo();
//            if (safetyUser != null) {
//                UserVo userVo = new UserVo();
//                BeanUtils.copyProperties(safetyUser, userVo);
//                BeanUtils.copyProperties(team, teamUserVo);
//                teamUserVo.setUserVo(userVo);
//            }
//            teamUserVos.add(teamUserVo);
        }
        return teamUserVos;
    }

    /**
     * 查看用户加入的队伍
     *
     * @param request 1
     * @return 200
     */
    @Override
    public List<TeamUserAvatarVo> getJoinTeamList(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        User user;
        try {
            user = UserUtils.getLoginUser(request);
        } catch (Exception e) {
            return null;
        }
        String userId = user.getId();
        List<TeamUserAvatarVo> userAvatarVos = baseMapper.selectJoinTeamUserList(userId);
        for (TeamUserAvatarVo userAvatarVo : userAvatarVos) {
            String voUserId = userAvatarVo.getUserId();
            if (userId.equals(voUserId)) {
                userAvatarVo.setCaptain(true);
            }
            userAvatarVo.setUserId(null);
        }
        return userAvatarVos;
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequest team
     * @param request         登录
     * @return v
     */
    @Override
    public boolean addUserTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        String teamId = teamJoinRequest.getTeamId();
        if (!StringUtils.hasText(teamId)) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        User loginUser = UserUtils.getLoginUser(request);
        Team team = this.getById(teamId);
        if (team == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        // 判断队伍的过期
        Date expireTime = team.getExpireTime();
        if (expireTime == null || expireTime.before(new Date())) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "队伍已过期");
        }
        // 判断队伍的权限
        Integer status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(statusEnum)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.ENCRYPTION.equals(statusEnum)) {
            if (!StringUtils.hasText(password) || !team.getPassword().equals(MD5.getTeamMD5(password))) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        StringBuilder stringBuilder = new StringBuilder("user::team::addUserTeam");
        RLock lock = redissonClient.getLock(stringBuilder.append(teamId).toString().intern());
        try {
            int i = 0;
            while (true) {
                i++;
                if (lock.tryLock(3000, 3000, TimeUnit.MILLISECONDS)) {
                    return getAddUserTeam(team, teamId, loginUser);
                }
                if (i > 30) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR, "加入失败");
                }
            }
        } catch (InterruptedException e) {
            log.error("加入队伍加锁失败。。。。。。");
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "加入失败");

        } finally {
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unlock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean getAddUserTeam(Team team, String teamId, User loginUser) {
        // 用户加入队伍不超过 5 个
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", loginUser.getId());
        long count = userTeamService.count(wrapper);
        if (count > 5) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "你最多加入 5 个队伍");
        }
        // 队伍已满
        count = countUserTeamByTeamId(teamId);
        if (count > team.getMaxNum()) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "队伍已满");
        }
        // 不能加入已经加入的队伍
        wrapper = new QueryWrapper<>();
        wrapper.eq("team_id", teamId);
        wrapper.eq("user_id", loginUser.getId());
        count = userTeamService.count(wrapper);
        if (count > 0) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "重复加入队伍");
        }
        // 保存
        UserTeam userTeam = new UserTeam();
        userTeam.setName(team.getName());
        userTeam.setUserId(loginUser.getId());
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }

    @Override
    @Transactional
    public void updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        Long maxNum = teamUpdateRequest.getMaxNum();
        String teamName = teamUpdateRequest.getName();
        String teamDescription = teamUpdateRequest.getDescription();
        if (maxNum != null) {
            if (maxNum < 1 || maxNum >= 20) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
            }
        }
        //队伍标题 <= 20
        if (StringUtils.hasText(teamName)) {
            if (teamName.length() >= 20) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
            }
        }
        //描述 <= 512
        if (StringUtils.hasText(teamDescription)) {
            if (teamDescription.length() >= 512) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍描述不满足要求");
            }
        }
        User loginUser = UserUtils.getLoginUser(request);
        String id = teamUpdateRequest.getId();
        if (!StringUtils.hasText(id)) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        Team team = this.getById(id);
        if (team == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍为空");
        }
        if (!loginUser.getId().equals(team.getUserId()) && !UserUtils.isAdmin(request)) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        Integer status = teamUpdateRequest.getStatus();
        if (status != null && status >= 0) {
            TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusByValue(status);
            String password = teamUpdateRequest.getPassword();
            if (statusEnum.equals(TeamStatusEnum.ENCRYPTION)) {
                if (!StringUtils.hasText(password)) {
                    throw new GlobalException(ErrorCode.NULL_ERROR, "请设置密码");
                }
            }
        }
        team = new Team();
        team.setId(teamUpdateRequest.getId());
        BeanUtils.copyProperties(teamUpdateRequest, team);
        if (StringUtils.hasText(team.getPassword())) {
            team.setPassword(MD5.getTeamMD5(team.getPassword()));
        }

        boolean b = this.updateById(team);
        if (!b) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "跟新失败");
        }
    }

    // 退出退伍
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(String teamId, HttpServletRequest request) {
        if (!StringUtils.hasText(teamId)) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        User loginUser = UserUtils.getLoginUser(request);

        Team team = this.getById(teamId);
        if (team == null || !StringUtils.hasText(team.getId())) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "没有该队伍");
        }
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        String userId = loginUser.getId();
        wrapper.eq("team_id", teamId);
        wrapper.eq("user_id", userId);
        long count = userTeamService.count(wrapper);
        if (count != 1) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "未加入队伍");
        }
        long teamHasJoinName = this.countUserTeamByTeamId(teamId);
        // 队伍只剩一人
        if (teamHasJoinName == 1) {
            boolean removeById = this.removeById(teamId);
            if (removeById) {
                boolean record = chatRecordService.deleteTeamChatRecordByTeamId(teamId);
                if (!record) {
                    throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除错误");
                }
            }
        } else {
            if (userId.equals(team.getUserId())) {
                // 队长 退出
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("team_id", teamId);
                userTeamQueryWrapper.last("order by join_time asc limit 2");
                List<UserTeam> list = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(list) || list.size() <= 1) {
                    throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
                }
                UserTeam userTeam = list.get(1);
                String teamUserId = userTeam.getUserId();
                // 跟新队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(teamUserId);
                boolean update = this.updateById(updateTeam);
                if (!update) {
                    throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "跟新队长失败");
                }
            }
        }
        // 移除用户
        return userTeamService.remove(wrapper);
    }

    /**
     * 根据 team ID 获取加入的人数
     *
     * @param teamId 队伍id
     * @return 数量
     */
    private long countUserTeamByTeamId(String teamId) {
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("team_id", teamId);
        return userTeamService.count(wrapper);

    }

    @Override
    public boolean quitTeamByUser(String teamId, String userId, HttpServletRequest request) {
        if (!StringUtils.hasText(teamId) && !StringUtils.hasText(userId)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "数据为空...");
        }
        User loginUser = UserUtils.getLoginUser(request);
        String loginUserId = loginUser.getId();
        Team team = baseMapper.selectById(teamId);
        if (team == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        String teamUserId = team.getUserId();
        if (!StringUtils.hasText(teamUserId)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        if (!loginUserId.equals(teamUserId)) {
            throw new GlobalException(ErrorCode.NO_AUTH, "权限不足...");
        }
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("team_id", teamId);
        UserTeam userTeam = userTeamService.getOne(wrapper);
        if (userTeam == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "人员不在队伍中....");
        }

        return userTeamService.removeById(userTeam);
    }

    @Override
    public List<String> getUserTeamListById(String teamId, String userId) {
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("team_id", teamId);
        List<UserTeam> list = userTeamService.list(wrapper);
        List<String> teamIdList = new ArrayList<>();
        if (list.size() <= 0) {
            return teamIdList;
        }
        list.forEach(userTeam -> {
            String teamUserId = userTeam.getUserId();
            if (!userId.equals(teamUserId)) {
                teamIdList.add(teamUserId);
            }
        });
        return teamIdList;
    }

    @Override
    public Team getTeamByTeamUser(String teamId, String userId) {
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("team_id", teamId);
        return this.getOne(wrapper);
    }

    @Override
    public boolean updateTeamByTeam(Team team) {
        if (team == null) {
            return false;
        }
        return this.updateById(team);
    }
}




