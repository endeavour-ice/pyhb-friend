package com.user.py.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.user.py.mode.entity.UserFriend;
import com.user.py.mode.entity.UserFriendReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
public interface UserFriendMapper extends BaseMapper<UserFriend> {

    List<UserFriendReq> selectCheckFriend(@Param("toUserId") String toUserId);

    int removeFromToUserId(@Param("fromUserId")String fromUserId,@Param("toUserId") String toUserId);

    int countFromToUserId(@Param("fromUserId")String fromUserId,@Param("toUserId") String toUserId);
}
