package com.user.py.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mapper.UserFriendMapper;
import com.user.py.mode.entity.ChatRecord;
import com.user.py.mode.entity.User;
import com.user.py.mode.entity.UserFriend;
import com.user.py.mode.entity.UserFriendReq;
import com.user.py.mode.entity.vo.UserVo;
import com.user.py.mode.request.AddFriendUSerUser;
import com.user.py.mode.request.RejectRequest;
import com.user.py.mode.resp.FriendUserResponse;
import com.user.py.service.IChatRecordService;
import com.user.py.service.IUserFriendReqService;
import com.user.py.service.IUserFriendService;
import com.user.py.service.IUserService;
import com.user.py.utils.UserUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
public class UserFriendServiceImpl extends ServiceImpl<UserFriendMapper, UserFriend> implements IUserFriendService {
    @Resource
    private IUserService userService;
    @Resource
    private IChatRecordService chatRecordService;
    @Resource
    private IUserFriendReqService userFriendReqService;

    @Override
    public List<User> selectFriend(String userId) {
        QueryWrapper<UserFriend> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).or().eq("friend_id", userId);
        List<UserFriend> userFriends = baseMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(userFriends)) {
            return null;
        }
        List<String> userIdByList = new ArrayList<>();
        userFriends.forEach(userFriend -> {
            if (userFriend.getUserId().equals(userId)) {
                userIdByList.add(userFriend.getFriendId());
            }
            if (userFriend.getFriendId().equals(userId)) {
                userIdByList.add(userFriend.getUserId());
            }
        });
        if (!CollectionUtils.isEmpty(userIdByList)) {
            return userService.listByIds(userIdByList);
        }
        return null;
    }

    @Override
    public FriendUserResponse getFriendUser(String friendId, HttpServletRequest request) {
        User loginUser = UserUtils.getLoginUser(request);
        if (!StringUtils.hasText(friendId)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "参数错误...");
        }
        String userId = loginUser.getId();
        UserFriend userFriend = this.getUserFriendByFriendId(friendId, userId);
        if (userFriend == null) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
        LocalDateTime createTime = userFriend.getCreateTime();
        Date date = Date.from(createTime.atZone(ZoneId.systemDefault()).toInstant());
        User feignUser = userService.getById(friendId);
        if (feignUser == null) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
        FriendUserResponse response = new FriendUserResponse();
        BeanUtils.copyProperties(feignUser, response);
        response.setCreateTime(date);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delFriendUser(String friendId, String userId) {
        if (!StringUtils.hasText(friendId)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "参数错误...");
        }
        UserFriend userFriend = this.getUserFriendByFriendId(friendId, userId);
        if (userFriend == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "参数错误...");
        }
        boolean removeById = this.removeById(userFriend);
        if (!removeById) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "参数错误...");
        }
        QueryWrapper<ChatRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).and(q -> q.eq("friend_id", friendId)).
                or().eq("user_id", friendId).and(q -> q.eq("friend_id", userId));
        long count = chatRecordService.count(wrapper);
        if (count > 0) {
            return chatRecordService.remove(wrapper);
        }


        return true;
    }

    private UserFriend getUserFriendByFriendId(String friendId, String userId) {
        QueryWrapper<UserFriend> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).and(q -> q.eq("friend_id", friendId)).
                or().eq("user_id", friendId).and(q -> q.eq("friend_id", userId));
        return this.getOne(wrapper);
    }

    @Override
    @Transactional
    public Long sendRequest(String userId, AddFriendUSerUser friendUSerUser) {
        if (friendUSerUser == null) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        String message = friendUSerUser.getMessage();
        String toUserId = friendUSerUser.getToUserId();
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(toUserId)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "数据为空,请重试");
        }
        if (userId.equals(toUserId)) {
            throw new GlobalException(ErrorCode.ERROR, "无法添加自己");
        }
        synchronized (userId.intern()) {
            User user = userService.getById(toUserId);
            if (user == null) {
                throw new GlobalException(ErrorCode.NULL_ERROR, "数据为空,请重试");
            }
            isUserFriend(userId, toUserId);
            QueryWrapper<UserFriendReq> wrapper = new QueryWrapper<>();
            wrapper.eq("from_userid", userId);
            wrapper.eq("to_userid", toUserId);
            long count = userFriendReqService.count(wrapper);
            if (count > 0) {
                throw new GlobalException(ErrorCode.NULL_ERROR, "以发送,等待同意中...");
            }
                int countFromToUserId = baseMapper.countFromToUserId(toUserId,userId);
            if (countFromToUserId > 0) {
                int con =baseMapper.removeFromToUserId(toUserId, userId);
                if (con <= 0) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR);
                } else {
                    UserFriend userFriend = new UserFriend();
                    userFriend.setUserId(toUserId);
                    userFriend.setFriendId(userId);
                    boolean save = this.save(userFriend);
                    if (!save) {
                        throw new GlobalException(ErrorCode.PARAMS_ERROR);
                    }else {
                        return 1L;
                    }
                }
            } else {

                UserFriendReq userFriendReq = new UserFriendReq();
                userFriendReq.setFromUserid(userId);
                userFriendReq.setToUserid(toUserId);
                if (StringUtils.hasText(message)) {
                    userFriendReq.setMessage(message);
                }
                boolean insert = userFriendReqService.save(userFriendReq);
                if (!insert) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR, "发送失败");
                }else {
                    return 0L;
                }
            }
        }


    }

    @Override
    public List<UserVo> checkFriend(String toUserId) {
        if (!StringUtils.hasText(toUserId)) {
            return null;
        }
        List<UserFriendReq> friendReqList = baseMapper.selectCheckFriend(toUserId);
        //List<UserFriendReq> friendReqList = userFriendReqService.list(wrapper);
        if (friendReqList == null || friendReqList.size() <= 0) {
            return null;
        }

        Map<String, List<UserFriendReq>> userFriendMap = friendReqList.stream().collect(Collectors.groupingBy(UserFriendReq::getFromUserid));
        List<User> users = userService.listByIds(userFriendMap.keySet());
        if (users.isEmpty()) {
            throw new RuntimeException("查找申请的用户为空");
        }
        return users.stream().map(user -> {
            UserVo userVo = UserUtils.getSafetyUser(user);
            userVo.setProfile(null);
            List<UserFriendReq> list = userFriendMap.get(userVo.getId());
            if (!CollectionUtils.isEmpty(list)) {
                String message = list.get(0).getMessage();
                if (StringUtils.hasText(message)) {
                    userVo.setProfile(message);
                }
            }
            return userVo;
        }).collect(Collectors.toList());

    }

    @Override
    @Transactional
    public boolean reject(RejectRequest rejectRequest, String userId) {
        if (rejectRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        synchronized (userId.intern()) {
            String acceptId = rejectRequest.getAcceptId();
            String refuseId = rejectRequest.getRefuseId();
            if (StringUtils.hasText(acceptId)) {
                int count = baseMapper.countFromToUserId(acceptId, userId);
                if (count <= 0) {
                    throw new GlobalException(ErrorCode.NULL_ERROR, "请求失败");
                }
                int toUserId = baseMapper.removeFromToUserId(acceptId, userId);
                if (toUserId <= 0) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR);
                } else {
                    isUserFriend(userId, acceptId);
                    UserFriend userFriend = new UserFriend();
                    userFriend.setUserId(userId);
                    userFriend.setFriendId(acceptId);
                    boolean save = this.save(userFriend);
                    if (!save) {
                        throw new GlobalException(ErrorCode.PARAMS_ERROR);
                    }
                }
            } else if (StringUtils.hasText(refuseId)) {
                int count = baseMapper.countFromToUserId(acceptId, userId);
                if (count <= 0) {
                    throw new GlobalException(ErrorCode.NULL_ERROR, "请求失败");
                }
                int toUserId = baseMapper.removeFromToUserId(refuseId, userId);
                if (toUserId <= 0) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR);
                }
            } else {
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
            }
        }
        return true;
    }

    private void isUserFriend(String userId, String friendId) {
        QueryWrapper<UserFriend> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).and(w -> w.eq("friend_id", friendId))
                .or().eq("user_id", friendId).and(w -> w.eq("friend_id", userId));
        long size = this.count(wrapper);
        if (size > 0) {
            throw new GlobalException(ErrorCode.ERROR, "重复添加好友");
        }
    }
}
