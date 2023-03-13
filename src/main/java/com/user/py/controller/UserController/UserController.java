package com.user.py.controller.UserController;


import cn.hutool.core.util.StrUtil;
import com.user.py.annotation.CurrentLimiting;
import com.user.py.common.B;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mode.entity.User;
import com.user.py.mode.entity.UserFriend;
import com.user.py.mode.entity.vo.UserVo;
import com.user.py.mode.request.*;
import com.user.py.mode.resp.SafetyUserResponse;
import com.user.py.mq.RabbitService;
import com.user.py.service.IUserFriendService;
import com.user.py.service.IUserService;
import com.user.py.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 缓存一致性 可以使用 Canal Java => https://blog.csdn.net/a56546/article/details/125170510
 * 数据库分库分表 读写分离 Mycat => https://blog.csdn.net/K_520_W/article/details/123702217
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author ice
 * @since 2022-06-14
 */
@RestController
@RequestMapping("/api/user")
@SuppressWarnings("all")
@Slf4j
public class UserController {
    @Autowired
    private IUserFriendService friendService;

    @Resource
    private IUserService userService;

    @Autowired(required = false)
    private RabbitService rabbitService;

    // 用户注册
    @PostMapping("/Register")
    public B<String> userRegister(@RequestBody UserRegisterRequest userRegister) {
        if (userRegister == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        String aLong = userService.userRegister(userRegister);
        if (StringUtils.hasText(aLong)) {
            UserFriend userFriend = new UserFriend();
            userFriend.setUserId(aLong);
            userFriend.setFriendId("1");
            boolean saveFriend = friendService.save(userFriend);
        }
        return B.ok(aLong);
    }

    /**
     * 获取当前的登录信息
     *
     * @return 返回用户
     */
    @GetMapping("/current")
    @CurrentLimiting
    public B<SafetyUserResponse> getCurrent(HttpServletRequest request) {
        User currentUser = UserUtils.getLoginUser(request);
        if (currentUser == null) {
            throw new GlobalException(ErrorCode.NO_LOGIN);
        }
        SafetyUserResponse safetyUser = userService.getCurrent(currentUser);
        // 进行脱敏
        return B.ok(safetyUser);
    }

    // 用户登录
    @PostMapping("/Login")
    public B<UserVo> userLogin(@RequestBody UserLoginRequest userLogin, HttpServletRequest request) {
        if (userLogin == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "数据为空!");
        }
        String userAccount = userLogin.getUserAccount();
        String password = userLogin.getPassword();
        boolean hasEmpty = StrUtil.hasEmpty(userAccount, password);
        if (hasEmpty) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "账号密码为空!");
        }
        UserVo user = userService.userLogin(userAccount, password, request);
        return B.ok(user);
    }

    // 忘记密码
    @PostMapping("/forget")
    public B<Boolean> userForget(@RequestBody UserRegisterRequest registerRequest) {
        boolean is = userService.userForget(registerRequest);
        return B.ok(is);
    }

    // 查询用户
    @GetMapping("/searchUser")
    @CurrentLimiting
    public B<Map<String, Object>> searchUser(@RequestParam(required = false) String username,
                                             @RequestParam(required = false) Long current, Long size,
                                             HttpServletRequest request) {
        Map<String, Object> user = userService.searchUser(request, username, current, size);
        return B.ok(user);
    }

    // 管理员删除用户
    @PostMapping("/delete")
    public B<Boolean> deleteUser(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        String ids;
        if (idRequest == null || !StringUtils.hasText(ids = idRequest.getId())) {
            return B.error(ErrorCode.NULL_ERROR);
        }
        int id = Integer.parseInt(ids);
        if (id <= 0) {
            return B.error(ErrorCode.NULL_ERROR);
        }
        boolean admin = userService.isAdmin(request);
        if (!admin) {
            return B.error(ErrorCode.NO_AUTH);
        }
        boolean removeById = userService.removeById(id);
        return B.ok(removeById);
    }

    /**
     * 修改用户
     *
     * @param user    要修改的数据
     * @param request
     * @return
     */
    @PostMapping("/UpdateUser")
    public B<Integer> UpdateUser(User user, HttpServletRequest request) {
        Integer is = userService.updateUser(user, request);
        return B.ok(is);
    }


    /**
     * 用户注销
     */
    @PostMapping("/Logout")
    public B<Integer> userLogout(HttpServletRequest request) {
        log.info("用户注销");
        if (request == null) {
            return B.error(ErrorCode.NULL_ERROR);
        }
        userService.userLogout(request);
        return B.ok();
    }


    @PostMapping("/search/tags/txt")
    public B<List<UserVo>> getSearchUserTag(@RequestBody UserSearchTagAndTxtRequest userSearchTagAndTxtRequest) {
        List<UserVo> userList = userService.searchUserTag(userSearchTagAndTxtRequest);
        return B.ok(userList);
    }

    /**
     * 修改用户
     */
    @PostMapping("/update")
    public B<Integer> updateUserByID(@RequestBody UpdateUserRequest updateUser, HttpServletRequest request) {
        if (request == null) {
            throw new GlobalException(ErrorCode.NO_LOGIN);
        }
        if (updateUser == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        int updataNum = userService.getUserByUpdateID(updateUser, request);
        return B.ok(updataNum);
    }

    /**
     * 主页展示数据
     */
    @GetMapping("/recommend")
    public B<Map<String, Object>> recommendUser(@RequestParam(required = false) long current, long size, HttpServletRequest request) {
        User loginUser = UserUtils.getLoginUser(request);
        Map<String, Object> map = userService.selectPageIndexList(current, size);
        return B.ok(map);
    }

    // 搜索用户
    @PostMapping("/searchUserName")
    public B<Map<String, Object>> searchUserName(@RequestBody UserSearchPage userSearchPage,
                                          HttpServletRequest request) {
        User user = UserUtils.getLoginUser(request);
        String userId = user.getId();
        if (userSearchPage == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        String userName = userSearchPage.getUserName();
        long pageNum = userSearchPage.getPageNum();
        long pageSize = userSearchPage.getPageSize();
        Map<String, Object> friendMap = userService.friendUserName(userId, userName,  pageNum, pageSize);
        return B.ok(friendMap);
    }

    /**
     * 根据单个标签搜索
     */
    @GetMapping("/searchUserTag")
    public B<List<UserVo>> searchUserTag(@RequestParam("tag") String tag, HttpServletRequest request) {
        List<UserVo> userList = userService.searchUserTag(tag, request);
        return B.ok(userList);
    }

    /**
     * 匹配用户
     *
     * @param num     推荐数量
     * @param request
     * @return
     */
    @GetMapping("/match")
    public B<List<UserVo>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "参数错误...");
        }
        List<UserVo> userVos = userService.matchUsers(num, request);
        return B.ok(userVos);
    }
}
