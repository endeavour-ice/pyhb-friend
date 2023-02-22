package com.user.py.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.domain.User;
import com.user.py.mode.domain.UserFriendReq;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
public interface IUserFriendReqService extends IService<UserFriendReq> {
    void sendRequest(String fromUserId, String toUserId);

    List<User> checkFriend(String userId);

    int Reject(String id);


}
