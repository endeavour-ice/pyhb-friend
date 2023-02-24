package com.user.py.service.impl;


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
import com.user.py.mode.constant.RedisKey;
import com.user.py.mode.constant.UserStatus;
import com.user.py.mode.domain.User;
import com.user.py.mode.domain.vo.UserAvatarVo;
import com.user.py.mode.domain.vo.UserVo;
import com.user.py.mode.request.UpdateUserRequest;
import com.user.py.mode.request.UserRegisterRequest;
import com.user.py.mode.request.UserSearchTagAndTxtRequest;
import com.user.py.mq.MqClient;
import com.user.py.mq.RabbitService;
import com.user.py.service.IUserService;
import com.user.py.utils.*;
import javafx.util.Pair;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Resource
    private RedisCache redisCache;
    @Resource
    private RabbitService rabbitService;


    @Override
    @Transactional
    public String userRegister(UserRegisterRequest userRegister) {
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
        String redisCode = redisCache.getCacheObject(RedisKey.redisRegisterCode + email);

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
        user.setAvatarUrl("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif?imageView2/1/w/80/h/80");
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
        rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.REMOVE_REDIS_KEY, RedisKey.redisIndexKey);
        return saveId;
    }

    @Override
    public UserVo userLogin(String userAccount, String password, HttpServletRequest request) {
        // 1. 校验
        if (userAccount.length() < 3) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (password.length() < 6) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 校验账户不能包含特殊字符
        if (REUtils.isName(userAccount)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "账户不能包含特殊字符");
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
        if (user.getUserStatus().equals(UserStatus.LOCKING)) {
            throw new GlobalException(ErrorCode.NO_AUTH, "该用户以锁定...");
        }
        UserVo cleanUser = UserUtils.getSafetyUser(user);
        // 记录用户的登录态
        HttpSession session = request.getSession();
        String token = JwtUtils.getJwtToken(user);
        session.setAttribute(USER_LOGIN_STATE, token);
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
        if (size > 300 || size <= 0) {
            size = 10;
        }
        Boolean hasKey = redisCache.hasKey(RedisKey.redisIndexKey);
        if (hasKey) {
            return redisCache.getCacheMap(RedisKey.redisIndexKey);
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("avatar_url", "user_account", "id", "tags", "profile");
        Page<User> commentPage = baseMapper.selectPage(new Page<>(current, size), wrapper);
        Map<String, Object> map = new HashMap<>();
        List<UserVo> userList = commentPage.getRecords().stream().map(UserUtils::getSafetyUser).collect(Collectors.toList());
        map.put("items", userList);
        map.put("current", commentPage.getCurrent());
        map.put("pages", commentPage.getPages());
        map.put("size", commentPage.getSize());
        map.put("total", commentPage.getTotal());
        map.put("hasNext", commentPage.hasNext());
        map.put("hasPrevious", commentPage.hasPrevious());
        boolean cacheMap = redisCache.setCacheMap(RedisKey.redisIndexKey, map, TimeUtils.getRemainSecondsOneDay(new Date()), TimeUnit.SECONDS);
        if (!cacheMap) {
            log.error("缓存失败。。。。。。。。。。。。。");
        }
        return map;
    }

    /**
     * 根据用户修改资料
     *
     * @return
     */
    @Override
    public int getUserByUpdateID(UpdateUserRequest updateUsers, HttpServletRequest request) {
        User loginUser = UserUtils.getLoginUser(request);
        String userId = updateUsers.getId();
        String username = updateUsers.getUsername();
        String gender = updateUsers.getGender();
        String tel = updateUsers.getTel();
        String email = updateUsers.getEmail();
        String profile = updateUsers.getProfile();
        String tags = updateUsers.getTags();
        if (!StringUtils.hasText(username) && !StringUtils.hasText(tel) &&
                !StringUtils.hasText(email) && !StringUtils.hasText(tags)
                && !StringUtils.hasText(gender) && !StringUtils.hasText(profile)) {
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
            String redisKey = RedisKey.redisFileByBingDingKey + email;
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
            String oldUserTags = oldUser.getTags();
            if (StringUtils.hasText(oldUserTags) && oldUserTags.equals(tags)) {
                throw new GlobalException(ErrorCode.NULL_ERROR, "重复提交...");
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
            rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.REMOVE_REDIS_KEY, RedisKey.redisIndexKey);
        } else {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "修改失败");
        }
        String token = JwtUtils.getJwtToken(loginUser);
        request.getSession().setAttribute(USER_LOGIN_STATE, token);
        return update;

    }

    public boolean isAdmin(User user) {

        return user != null && Objects.equals(user.getRole(), ADMIN_ROLE);
    }

    @Override
    public List<UserVo> friendUserName(String userID, String friendUserName) {
        if (!StringUtils.hasText(friendUserName)) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.like("user_account", friendUserName);
        List<User> userList = baseMapper.selectList(userQueryWrapper);
        if (userList.size() == 0) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "查无此人");
        }
        userList = userList.stream().filter(user -> !userID.equals(user.getId())).collect(Collectors.toList());
        return userList.stream().map(UserUtils::getSafetyUser).collect(Collectors.toList());

    }


    public boolean TagsUtil(String userId) {
        String tagKey = RedisKey.tagRedisKey + userId;
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
        rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.REMOVE_REDIS_KEY, RedisKey.redisIndexKey);
        return true;
    }

    @Override
    public Map<String, Object> searchUser(HttpServletRequest request, String username, Long current, Long size) {
        boolean admin = this.isAdmin(request);
        if (!admin) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "你不是管理员");
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        // 如果name有值
        if (StringUtils.hasText(username)) {
            wrapper.like("username", username);
        }
        if (current == null || size == null) {
            current = 1L;
            size = 30L;
        }
        if (size > 30L) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "请求数据有误");
        }
        Page<User> page = new Page<>(current, size);
        Page<User> userPage = baseMapper.selectPage(page, wrapper);
        // 通过stream 流的方式将列表里的每个user进行脱敏
        List<UserVo> userVoList = userPage.getRecords().stream().map(UserUtils::getSafetyUser).collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put("records", userVoList);
        map.put("current", userPage.getCurrent());
        map.put("total", userPage.getTotal());
        return map;
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
        String redisCode = redisCache.getCacheObject(RedisKey.redisForgetCode + email);
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
}
