package com.user.py.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.user.py.common.ErrorCode;
import com.user.py.designPatten.singleton.GsonUtils;
import com.user.py.exception.GlobalException;
import com.user.py.mapper.UserMapper;
import com.user.py.mode.constant.CacheConstants;
import com.user.py.mode.dto.PageFilter;
import com.user.py.mode.entity.User;
import com.user.py.mode.entity.UserLabel;
import com.user.py.mode.entity.vo.UserAvatarVo;
import com.user.py.mode.entity.vo.UserVo;
import com.user.py.mode.enums.UserStatus;
import com.user.py.mode.request.*;
import com.user.py.mode.resp.SafetyUserResponse;
import com.user.py.mq.MqClient;
import com.user.py.mq.RabbitService;
import com.user.py.service.IUserLabelService;
import com.user.py.service.IUserService;
import com.user.py.utils.*;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.user.py.mode.constant.UserConstant.ADMIN_ROLE;
import static com.user.py.mode.constant.UserConstant.USER_LOGIN_STATE;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author ice
 * @since 2022-06-14
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Resource
    private RedisCache redisCache;
    @Resource
    private RabbitService rabbitService;
    @Resource
    private IUserLabelService labelService;
    @Autowired
    private RedisIndexedSessionRepository sessionRepository;


    @Override
    public SafetyUserResponse getCurrent(HttpServletRequest request) {
        User currentUser = UserUtils.getLoginUser(request);
        return UserUtils.getSafetyUserResponse(currentUser);
    }

    @Override
    @Transactional
    public void userRegister(UserRegisterRequest userRegister) {
        String userAccount = userRegister.getUserAccount();
        String password = userRegister.getPassword();
        String checkPassword = userRegister.getCheckPassword();
        String planetCode = userRegister.getPlanetCode();
        String code = userRegister.getCode();
        String email = userRegister.getEmail();


        if (!StringUtils.hasText(planetCode)) {
            planetCode = RandomUtil.randomInt(10, 10000) + "";
        }
        boolean hasEmpty = StrUtil.hasEmpty(userAccount, password, checkPassword, planetCode);
        if (hasEmpty) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        // 1. 校验
        if (StrUtil.hasEmpty(userAccount, password, checkPassword, planetCode)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 3) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户名过短");
        }

        if (password.length() < 6 || checkPassword.length() < 6) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "密码过短");
        }
        if (planetCode.length() > 5) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "编号过长");
        }
        // 校验账户不能包含特殊字符

        if (REUtils.isName(userAccount)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "账号特殊符号");
        }
        // 判断密码和和用户名是否相同
        if (password.equals(userAccount)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "账号密码相同");
        }
        if (!password.equals(checkPassword)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "确认密码错误");
        }

        if (!StringUtils.hasText(email)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "邮箱为空");
        }
        email = email.toLowerCase();
        if (REUtils.isEmail(email)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "请输入正确邮箱");
        }
        if (!StringUtils.hasText(code)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "验证码为空");
        }
        String redisCode = redisCache.getCacheObject(CacheConstants.REDIS_REGISTER_CODE + email);

        if (!StringUtils.hasText(redisCode) || !code.equals(redisCode)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "验证码错误，请重试");
        }
        // 判断用户是否重复
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_account", userAccount);
        Long aLong = baseMapper.selectCount(wrapper);
        if (aLong > 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "注册用户重复");
        }
        // 判断用户是否重复
        wrapper = new QueryWrapper<>();
        wrapper.eq("planet_code", planetCode);
        Long a = baseMapper.selectCount(wrapper);
        if (a > 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "注册用户编号重复");
        }
        wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        Long count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "注册邮箱重复");
        }
        // 加密密码
        String passwordMD5 = MD5.getMD5(password);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(passwordMD5);
        user.setPlanetCode(planetCode);
        user.setAvatarUrl(AvatarUrlUtils.getRandomUrl());
        user.setUsername(userAccount);
        user.setEmail(email);
        boolean save = this.save(user);

        if (!save) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "注册用户失败");
        }
        String saveId = user.getId();
        if (!StringUtils.hasText(saveId)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "注册用户失败");
        }
        long id = IdUtil.getSnowflakeNextId();
        int is = baseMapper.saveFriend(String.valueOf(id), saveId, "1");
        if (is != 1) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "注册用户失败");
        }

    }

    @Override
    public UserVo userLogin(String userAccount, String password, String code, String uuid, HttpServletRequest request) {
        if (!StringUtils.hasText(uuid) || !StringUtils.hasText(uuid)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        if (!StringUtils.hasText(userAccount) || !StringUtils.hasText(userAccount)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "账号密码为空");
        }
        // 1. 校验
        if (userAccount.length() <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (password.length() < 6) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 校验账户不能包含特殊字符
        if (REUtils.isName(userAccount)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "账户不能包含特殊字符");
        }
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        String codeImage = redisCache.getCacheObject(verifyKey);
        if (!StringUtils.hasText(codeImage)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        if (!code.equals(codeImage)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        String passwordMD5 = MD5.getMD5(password);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_account", userAccount);
        wrapper.eq("password", passwordMD5);
        User user = baseMapper.selectOne(wrapper);

        if (user == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "账号密码错误");
        }

        // 用户脱敏
        if (user.getUserStatus().equals(UserStatus.LOCKING.getKey())) {
            throw new GlobalException(ErrorCode.NO_AUTH, "以封号，请联系管理员");
        }


        String id = request.getSession().getId();
        String isId = CacheConstants.IS_LOGIN + user.getId();
        if (redisCache.hasKey(isId)) {
            String sessionId = redisCache.getCacheObject(isId);
            if (!id.equals(sessionId)) {
                sessionRepository.deleteById(sessionId);
            }
        }
        UserVo cleanUser = UserUtils.getSafetyUser(user);
        // 记录用户的登录态
        HttpSession session = request.getSession();
        String token = JwtUtils.getJwtToken(user);
        session.setAttribute(USER_LOGIN_STATE, token);
        redisCache.setCacheObject(isId, id, 86400, TimeUnit.SECONDS);
        return cleanUser;
    }


    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员查询
        User user = JwtUtils.getMemberIdByJwtToken(request);
        return user != null && Objects.equals(user.getRole(), ADMIN_ROLE);
    }

    /**
     * 用户注销
     *
     * @param request 1
     */
    @Override
    public void userLogout(HttpServletRequest request) {
        User loginUser = UserUtils.getLoginUser(request);
        String loginUserId = loginUser.getId();
        String key = CacheConstants.IS_LOGIN + loginUserId;
        redisCache.deleteObject(key);
        request.getSession().removeAttribute(USER_LOGIN_STATE);
    }

    /**
     * 修改用户
     *
     * @param user
     * @return
     */
    @Override
    public Integer updateUser(User user, HttpServletRequest request) {
        if (user == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        boolean admin = isAdmin(request);
        if (!admin) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "你不是管理员");
        }
        int update = baseMapper.updateById(user);
        if (update <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "修改失败");
        }
        return update;
    }


    /**
     * ===============================================================
     * 根据标签搜索用户
     *
     * @return 返回用户列表
     */
    @Override
    public List<UserVo> searchUserTag(UserSearchTagAndTxtRequest userSearchTagAndTxtRequest) {
        if (userSearchTagAndTxtRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请求数据为空");
        }

        List<String> tagNameList = userSearchTagAndTxtRequest.getTagNameList();
        String searchTxt = userSearchTagAndTxtRequest.getSearchTxt();
        if (!StringUtils.hasText(searchTxt) && CollectionUtils.isEmpty(tagNameList)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请求数据为空");
        }
        // sql 语句查询
//        QueryWrapper<User> wrapper = new QueryWrapper<>();
//        // 拼接and 查询
//        for (String tagName : tagNameList) {
//            wrapper = wrapper.like("tags", tagName);
//        }
//        List<User> userList = baseMapper.selectList(wrapper);
        // 内存查询
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(searchTxt)) {
            wrapper.and(wq -> wq.like("username", searchTxt).or().like("user_account", searchTxt)
                    .or().like("gender", searchTxt).or().like("tel", searchTxt).or().like("email", searchTxt).
                    like("profile", searchTxt));
        }
        List<User> userList = baseMapper.selectList(wrapper);
        if (userList.size() <= 0) {
            return new ArrayList<>();
        }

        if (!CollectionUtils.isEmpty(tagNameList)) {
            Gson gson = GsonUtils.getGson();
            return userList.stream().filter(user -> {
                String tagStr = user.getTags();
                // 将json 数据解析成 Set
                Set<String> tempTagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>() {
                }.getType());
                tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
                for (String tagName : tagNameList) {
                    if (tempTagNameSet.contains(tagName)) {
                        return true;
                    }
                }
                return false;
            }).map(UserUtils::getSafetyUser).collect(Collectors.toList());
        }

        return userList.stream().map(UserUtils::getSafetyUser).collect(Collectors.toList());
    }


    @Override
    public Map<String, Object> selectPageIndexList(long current, long size) {
        PageFilter filter = new PageFilter(current, size);
        current = filter.getCurrent();
        size = filter.getSize();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_status", UserStatus.NORMAL.getKey());
        long count = this.count(wrapper);
        List<UserVo> userVoList = baseMapper.selectUserVoList(current, size, UserStatus.NORMAL.getKey());
        HashMap<String, Object> map = new HashMap<>();
        map.put("items", userVoList);
        map.put("total", count);
        return map;
    }

    /**
     * 根据用户修改资料
     *
     * @return
     */
    @Override
    public UserVo getUserByUpdateID(UpdateUserRequest updateUsers, HttpServletRequest request) {
        User loginUser = UserUtils.getLoginUser(request);
        String userId = updateUsers.getId();
        String username = updateUsers.getUsername();
        String gender = updateUsers.getGender();
        String tel = updateUsers.getTel();
        String email = updateUsers.getEmail();
        String profile = updateUsers.getProfile();
        String tags = updateUsers.getTags();
        String status = updateUsers.getStatus();
        if (StringUtils.hasText(status) && UserStatus.PRIVATE.getName().equals(status)) {
            loginUser.setUserStatus(UserStatus.PRIVATE.getKey());
        }
        if (!StringUtils.hasText(username) && !StringUtils.hasText(tel) &&
                !StringUtils.hasText(email) && !StringUtils.hasText(tags)
                && !StringUtils.hasText(gender) && !StringUtils.hasText(profile) && !StringUtils.hasText(status)) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        if (StringUtils.hasText(username)) {
            // 将特殊字符替换为空字符串
            String regEx = "\\pP|\\pS|\\s+";
            username = Pattern.compile(regEx).matcher(username).replaceAll("").trim();
            loginUser.setUsername(username);
        }
        if (!StringUtils.hasText(userId) || Long.parseLong(userId) <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.hasText(profile)) {
            if (profile.length() >= 200) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "描述过长");
            } else if (profile.equals(loginUser.getProfile())) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "重复设置");
            }
            loginUser.setProfile(profile);
        }
        if (StringUtils.hasText(gender)) {
            if (!"男".equals(gender) && !"女".equals(gender)) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR);
            } else if (gender.equals(loginUser.getGender())) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "重复设置");
            }
            loginUser.setGender(gender);
        }
        if (!isAdmin(loginUser) && !userId.equals(loginUser.getId())) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        User oldUser = baseMapper.selectById(userId);
        if (oldUser == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        if (StringUtils.hasText(tel)) {
            if (tel.equals(loginUser.getTel())) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "重复设置");
            }
            if (REUtils.isTel(tel)) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
            }
            loginUser.setTel(tel);
        }
        if (StringUtils.hasText(email)) {
            email = email.toLowerCase();
            if (this.seeUserEmail(email)) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "该邮箱已被注册");
            }
            String updateUsersCode = updateUsers.getCode();
            if (!StringUtils.hasText(updateUsersCode)) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "验证码错误");
            }
            String redisKey = CacheConstants.REDIS_FILE_BY_BING_DING_KEY + email;
            String code = redisCache.getCacheObject(redisKey);
            if (!StringUtils.hasText(code)) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "验证码错误");
            }
            if (!updateUsersCode.equals(code)) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "验证码错误");
            }
        }
        // 设置标签
        if (StringUtils.hasText(tags)) {
            Gson gson = GsonUtils.getGson();
            List<String> tagIds;
            String oldUserTags;
            try {
                tagIds = gson.fromJson(tags, new TypeToken<List<String>>() {
                }.getType());
                List<UserLabel> userLabels = labelService.listByIds(tagIds);
                if (userLabels.isEmpty()) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR);
                }
                List<String> tagList = userLabels.stream().map(UserLabel::getLabel).collect(Collectors.toList());
                tags = gson.toJson(tagList);
                oldUserTags = oldUser.getTags();
            } catch (Exception e) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "修改失败,请重试");
            }

            if (StringUtils.hasText(oldUserTags) && oldUserTags.equals(tags)) {
                throw new GlobalException(ErrorCode.NULL_ERROR, "重复提交");
            }
            boolean isBoolean = this.TagsUtil(userId);
            if (isBoolean) {
                loginUser.setTags(tags);
            } else {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "修改失败,请重试");
            }
        }
        int update = baseMapper.updateById(loginUser);
        if (update > 0) {
            rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.REMOVE_REDIS_KEY, CacheConstants.REDIS_INDEX_KEY);
        } else {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "修改失败,请重试");
        }
        String token = JwtUtils.getJwtToken(loginUser);
        request.getSession().setAttribute(USER_LOGIN_STATE, token);

        return UserUtils.getSafetyUser(loginUser);

    }

    public boolean isAdmin(User user) {

        return user != null && Objects.equals(user.getRole(), ADMIN_ROLE);
    }

    @Override
    public Map<String, Object> friendUserName(User user, UserSearchPage userSearchPage) {
        String userName = userSearchPage.getUserName();
        if (!StringUtils.hasText(userName)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "请输入账号");
        }
        String id = user.getId();
        PageFilter pageFilter = new PageFilter(userSearchPage.getPageNum(), userSearchPage.getPageSize());
        long current = pageFilter.getCurrent();
        long size = pageFilter.getSize();
        List<UserVo> userVoList = getSearchUserByPage(current, size, userName);
        userVoList = userVoList.stream().filter(userVo -> !userVo.getId().equals(id) && userVo.getUserStatus() != UserStatus.PRIVATE.getKey()).collect(Collectors.toList());
        long total = baseMapper.getUserCount();
        Map<String, Object> map = new HashMap<>();
        map.put("current", current + 1);
        map.put("records", userVoList);
        map.put("total", total);
        return map;
    }


    public boolean TagsUtil(String userId) {
        String tagKey = CacheConstants.TAG_REDIS_KEY + userId;
        Integer tagNum = redisCache.getCacheObject(tagKey);
        if (tagNum == null) {
            return redisCache.setCacheObject(tagKey, 1,
                    TimeUtils.getRemainSecondsOneDay(new Date()), TimeUnit.SECONDS);
        }
        if (tagNum > 5) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "今天修改次数以上限...");
        }
        try {
            redisCache.increment(tagKey);
        } catch (Exception e) {
            return false;
        }
        rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.REMOVE_REDIS_KEY, CacheConstants.REDIS_INDEX_KEY);
        return true;
    }


    /**
     * 根据名字查找
     *
     * @param current
     * @param size
     * @param userAccount
     * @return
     */
    private List<UserVo> getSearchUserByPage(Long current, Long size, String userAccount) {
        return baseMapper.selectFindByUserAccountLikePage(current, size, userAccount);
    }

    /**
     * 搜索用户的标签
     *
     * @param tag     标签
     * @param request 登录的请求
     * @return 返回标签
     */
    @Override
    public List<UserVo> searchUserTag(String tag, HttpServletRequest request) {
        if (!StringUtils.hasText(tag)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        UserUtils.getLoginUser(request);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("tags", tag);
        Page<User> commentPage = baseMapper.selectPage(new Page<>(1, 200), wrapper);
        List<User> list = commentPage.getRecords();
        return list.parallelStream().map(UserUtils::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 通过编辑距离算法 推荐用户
     *
     * @param num     推荐的数量
     * @param request 登录
     * @return 返回
     */
    @Override
    public List<UserVo> matchUsers(long num, HttpServletRequest request) {
        User loginUser = UserUtils.getLoginUser(request);
        String tags = loginUser.getTags();
        if (!StringUtils.hasText(tags)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请先设置标签");
        }
        Gson gson = GsonUtils.getGson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
//        SortedMap<Integer, User> indexDistanceMap = new TreeMap<>();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("id", "tags");
        wrapper.isNotNull("tags");
        List<User> userList = this.list(wrapper);
        List<Pair<Integer, String>> pairs = new ArrayList<>();
        for (User user : userList) {
            String userTags = user.getTags();
            if (!StringUtils.hasText(userTags) || user.getId().equals(loginUser.getId())) {
                continue;
            }
            List<String> tagUserList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            int distance = AlgorithmUtils.minDistance(tagList, tagUserList);
            int size = pairs.size();
            if (size - 1 >= num) {
                pairs.sort(Comparator.comparingInt(Pair::getKey));
                Pair<Integer, String> pair = pairs.get(size - 1);
                Integer key = pair.getKey();
                if (distance >= key) {
                    continue;
                }
                pairs.set(size - 1, new Pair<>(distance, user.getId()));

            } else {
                pairs.add(new Pair<>(distance, user.getId()));
            }
        }
        List<UserVo> findUserList = new ArrayList<>();
        if (pairs.size() > 0) {
            List<String> userIds = pairs.stream().map(Pair::getValue).collect(Collectors.toList());
            List<User> users = this.listByIds(userIds);
            if (users == null || users.size() <= 0) {
                return findUserList;
            }
            // 用户id进行分组
            Map<String, List<UserVo>> userListByUserIdMap = users.stream().map(UserUtils::getSafetyUser).collect(Collectors.groupingBy(UserVo::getId));

            for (String userId : userIds) {
                findUserList.add(userListByUserIdMap.get(userId).get(0));
            }
        }
        return findUserList;
//        return indexDistanceMap.keySet().parallelStream().map(indexDistanceMap::get).limit(num).collect(Collectors.toList());
    }

    @Override
    public boolean userForget(UserRegisterRequest registerRequest) {
        if (registerRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "账号为空");
        }
        String userAccount = registerRequest.getUserAccount();
        String email = registerRequest.getEmail();
        String code = registerRequest.getCode();
        String password = registerRequest.getPassword();
        String checkPassword = registerRequest.getCheckPassword();
        if (!StringUtils.hasText(userAccount)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "账号为空");
        }
        if (!StringUtils.hasText(email)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "邮箱为空");
        }
        if (!StringUtils.hasText(code)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "验证码为空");
        }
        if (!StringUtils.hasText(password)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "密码为空");
        }
        if (!StringUtils.hasText(checkPassword)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "确认密码为空");
        }
        email = email.toLowerCase();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        User user = this.getOne(wrapper);
        if (user == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "该邮箱没有注册过");
        }
        String userUserAccount = user.getUserAccount();
        if (!userAccount.equals(userUserAccount)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请输入该邮箱绑定的账号");
        }
        String redisCode = redisCache.getCacheObject(CacheConstants.REDIS_FORGET_CODE + email);
        if (!StringUtils.hasText(redisCode)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "验证码已过期请重试");
        }
        if (!code.equals(redisCode)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "验证码错误");
        }
        String md5 = MD5.getMD5(password);
        user.setPassword(md5);
        boolean u = this.updateById(user);
        if (!u) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "修改错误请刷新重试");
        }
        return true;
    }

    @Override
    public boolean seeUserEmail(String email) {
        if (StringUtils.hasText(email)) {
            email = email.toLowerCase();
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("email", email);
            long count = this.count(wrapper);
            return count >= 1;

        }
        return false;
    }

    // 根据邮箱查找用户
    @Override
    public User forgetUserEmail(String email) {
        if (StringUtils.hasText(email)) {
            email = email.toLowerCase();
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.select("user_account", "email");
            wrapper.eq("email", email);
            return this.getOne(wrapper);
        }
        return null;
    }

    @Override
    public List<UserAvatarVo> getUserAvatarVoByIds(List list) {
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        return baseMapper.getUserAvatarVoByIds(ListUtil.ListToString(list));
    }

    @Override
    public UserVo getUserVoByNameOrId(IdNameRequest idNameRequest, User loginUser) {
        String id = idNameRequest.getId();
        String name = idNameRequest.getName();
        if (!StringUtils.hasText(id) && !StringUtils.hasText(name)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.hasText(id)) {
            if (id.equals(loginUser.getId())) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "你不能查找自己");
            }
            User user = this.getById(id);
            return UserUtils.getSafetyUser(user);
        } else {
            return baseMapper.selectByNameLike(name);

        }
    }
}
