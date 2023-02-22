package com.user.py.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.domain.User;
import com.user.py.mode.domain.UserFriend;
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
     * 接收好友请求
     * @param reqId
     */
    void addFriendReq(String reqId,String userId);

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
}
