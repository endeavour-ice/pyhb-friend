package com.user.py.controller.PartnerController;


import com.user.py.common.B;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mode.domain.User;
import com.user.py.mode.domain.vo.UserVo;
import com.user.py.mode.request.AddFriendUSerUser;
import com.user.py.mode.resp.FriendUserResponse;
import com.user.py.service.IUserFriendReqService;
import com.user.py.service.IUserFriendService;
import com.user.py.utils.UserUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
@Log4j2
public class UserFriendController {
    @Autowired
    private IUserFriendService friendService;
    @Autowired
    private IUserFriendReqService friendReqService;


    // 添加好友
    @PostMapping("/friendUser")
    @Transactional
    public B<String> friendRequest(@RequestBody AddFriendUSerUser addFriendUSerUser, HttpServletRequest request) {
        if (addFriendUSerUser == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        String id = addFriendUSerUser.getToUserId();
        User loginUser = UserUtils.getLoginUser(request);
        String userId = loginUser.getId();
        friendReqService.sendRequest(userId, id);
        return B.ok();
    }


    // 查看好友申请
    @GetMapping("/checkFriend")
    public B<List<UserVo>> CheckFriendRequests(HttpServletRequest request) {
        User user = UserUtils.getLoginUser(request);
        String userId = user.getId();
        List<UserVo> users = friendReqService.checkFriend(userId);
        return B.ok(users);
    }

    /**
     * 拒绝好友 TODO 将接受好友合并
     * @param id
     * @return
     */
    @GetMapping("/rejectFriend")
    public B<Integer> rejectFriend(@RequestParam(required = false) String id) {
        log.info("拒绝好友");
        if (!StringUtils.hasLength(id)) {
            return B.error(ErrorCode.NULL_ERROR);
        }
        int i = friendReqService.Reject(id);
        if (i <= 0) {
            return B.error(ErrorCode.SYSTEM_EXCEPTION);
        }
        return B.ok(i);
    }


    /**
     * 接收好友请求
     */
    @GetMapping("/acceptFriendReq")
    public B<String> acceptFriendReq(@RequestParam(required = false) String reqId, HttpServletRequest request) {

        User user = UserUtils.getLoginUser(request);
        int reject = friendReqService.Reject(reqId);
        if (reject <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "添加好友失败");
        }
        String userId = user.getId();
        friendService.addFriendReq(reqId, userId);


        return B.ok();
    }


    /**
     * 查找好友
     */
    @GetMapping("/selectFriendList")
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
    @GetMapping("/delFriendUser")
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
