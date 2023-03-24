package com.user.py.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.entity.User;
import com.user.py.mode.entity.UserFriend;
import com.user.py.mode.entity.vo.UserVo;
import com.user.py.mode.request.AddFriendUSerUser;
import com.user.py.mode.request.RejectRequest;
import com.user.py.mode.resp.FriendUserResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
public interface IUserFriendService extends IService<UserFriend> {


    /**
     * 查找好友
     * @param userId
     * @return
     */
    List<User> selectFriend(String userId);

    FriendUserResponse getFriendUser(String friendId, HttpServletRequest request);

    /**
     * 删除好友
     * @param friendId
     * @param userId
     * @return
     */
    boolean delFriendUser(String friendId, String userId);

    Long sendRequest(String fromUserId, AddFriendUSerUser toUserId);

    List<UserVo> checkFriend(String userId);

    boolean reject(RejectRequest rejectRequest, String userId);
}
