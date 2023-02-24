package com.user.py.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mapper.UserFriendReqMapper;
import com.user.py.mode.constant.RedisKey;
import com.user.py.mode.domain.User;
import com.user.py.mode.domain.UserFriend;
import com.user.py.mode.domain.UserFriendReq;
import com.user.py.mode.domain.vo.UserVo;
import com.user.py.mq.MqClient;
import com.user.py.mq.RabbitService;
import com.user.py.service.IUserFriendReqService;
import com.user.py.service.IUserFriendService;
import com.user.py.service.IUserService;
import com.user.py.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
@Service
public class UserFriendReqServiceImpl extends ServiceImpl<UserFriendReqMapper, UserFriendReq> implements IUserFriendReqService {
    @Autowired
    private IUserService userService;
    @Autowired
    private IUserFriendService userFriendService;

    @Resource
    private RabbitService rabbitService;

    @Override
    public  void sendRequest(String userId, String toUserId) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(toUserId)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "数据为空,请重试");
        }
        if (userId.equals(toUserId)) {
            throw new GlobalException(ErrorCode.ERROR, "无法添加自己");
        }
        User user = userService.getById(toUserId);
        if (user == null) {
            throw new GlobalException(ErrorCode.ERROR, "人员错误");
        }
        QueryWrapper<UserFriend> friendQueryWrapper = new QueryWrapper<>();
        friendQueryWrapper.eq("user_id", userId).and(w -> w.eq("friends_id", toUserId))
                .or().eq("user_id", toUserId).and(w -> w.eq("friends_id", userId));
        long size = userFriendService.count(friendQueryWrapper);
        if (size > 0) {
            throw new GlobalException(ErrorCode.ERROR, "重复添加好友");
        }
        QueryWrapper<UserFriendReq> wrapper = new QueryWrapper<>();
        wrapper.eq("from_userid", userId);
        wrapper.eq("to_userid", toUserId);
        Long count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "以发送");
        }
        UserFriendReq userFriendReq = new UserFriendReq();
        userFriendReq.setFromUserid(userId);
        userFriendReq.setToUserid(toUserId);
        int insert = baseMapper.insert(userFriendReq);
        if (insert != 1) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "发送失败");
        }
        String redisKey = RedisKey.selectFriend + userId;
        rabbitService.sendMessage(MqClient.DIRECT_EXCHANGE, MqClient.REMOVE_REDIS_KEY, redisKey);
    }

    @Override
    public List<UserVo> checkFriend(String userId) {
        QueryWrapper<UserFriendReq> wrapper = new QueryWrapper<>();
        wrapper.eq("to_userid", userId);
        List<UserFriendReq> friendReqList = baseMapper.selectList(wrapper);
        if (friendReqList == null || friendReqList.size() <= 0) {
            return null;
        }
        friendReqList = friendReqList.stream().filter(userFriendReq -> userFriendReq.getUserStatus() == 0).collect(Collectors.toList());
        if (friendReqList.isEmpty()) {
            return null;
        }

        ArrayList<String> list = new ArrayList<>();
        for (UserFriendReq userFriendReq : friendReqList) {
            String fromUserid = userFriendReq.getFromUserid();
            list.add(fromUserid);
        }
        List<User> users = userService.listByIds(list);
        if (users.isEmpty()) {
            throw new RuntimeException("查找申请的用户为空");
        }
        return users.stream().map(UserUtils::getSafetyUser).collect(Collectors.toList());

    }

    @Override
    public int Reject(String id) {
        UserFriendReq userFriendReq = new UserFriendReq();
        QueryWrapper<UserFriendReq> wrapper = new QueryWrapper<>();
        userFriendReq.setUserStatus(1);
        wrapper.eq("from_userid", id);
        return baseMapper.update(userFriendReq, wrapper);
    }


}
