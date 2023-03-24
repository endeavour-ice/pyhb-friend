package com.user.py.controller.PartnerController;


import com.user.py.annotation.AuthSecurity;
import com.user.py.common.B;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mode.entity.User;
import com.user.py.mode.entity.vo.UserVo;
import com.user.py.mode.enums.UserRole;
import com.user.py.mode.request.AddFriendUSerUser;
import com.user.py.mode.request.RejectRequest;
import com.user.py.mode.resp.FriendUserResponse;
import com.user.py.service.IUserFriendService;
import com.user.py.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
@RestController
@RequestMapping("/partner/friend/userFriend")
//@CrossOrigin(origins = {"http://localhost:7777"}, allowCredentials = "true")
@Slf4j
public class UserFriendController {
    @Autowired
    private IUserFriendService friendService;


    // 添加好友
    @PostMapping("/friend")
    public B<Long> friendRequest(@RequestBody AddFriendUSerUser addFriendUSerUser, HttpServletRequest request) {
        if (addFriendUSerUser == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }

        User loginUser = UserUtils.getLoginUser(request);
        String userId = loginUser.getId();
        Long aLong = friendService.sendRequest(userId, addFriendUSerUser);
        return B.ok(aLong);
    }


    // 查看好友申请
    @GetMapping("/check")
    public B<List<UserVo>> CheckFriendRequests(HttpServletRequest request) {
        User user = UserUtils.getLoginUser(request);
        String userId = user.getId();
        List<UserVo> users = friendService.checkFriend(userId);
        return B.ok(users);
    }

    /**
     * 接受和拒绝好友
     *
     * @param rejectRequest
     * @return
     */
    @PostMapping("/reject")
    public B<Boolean> rejectFriend(@RequestBody(required = false) RejectRequest rejectRequest, HttpServletRequest request) {
        User loginUser = UserUtils.getLoginUser(request);
        boolean i = friendService.reject(rejectRequest, loginUser.getId());
        return B.ok();
    }


    /**
     * 查找好友
     */
    @GetMapping("/select")
    public B<List<User>> selectFriendList(HttpServletRequest request) {
        User user = UserUtils.getLoginUser(request);
        String userId = user.getId();
        List<User> userList = friendService.selectFriend(userId);

        return B.ok(userList);
    }

    /**
     * 查看好友详情
     *
     * @param friendId
     * @return
     */
    @GetMapping("/getFriendUser")
    public B<FriendUserResponse> getFriendUser(@RequestParam("friendId") String friendId, HttpServletRequest request) {
        FriendUserResponse friendUser = friendService.getFriendUser(friendId, request);
        return B.ok(friendUser);
    }

    /**
     * 删除好友
     */
    @GetMapping("/del")
    @AuthSecurity(isNoRole = {UserRole.TEST})
    public B<Boolean> delFriendUser(@RequestParam("friendId") String friendId, HttpServletRequest request) {
        User loginUser = UserUtils.getLoginUser(request);
        String userId = loginUser.getId();
        boolean is = friendService.delFriendUser(friendId, userId);
        if (!is) {
            return B.error();
        }

        return B.ok();
    }
}
