package com.user.py.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.user.py.mode.entity.ChatRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 聊天记录表 Mapper 接口
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */

public interface ChatRecordMapper extends BaseMapper<ChatRecord> {


    int updateReadBatchById(@Param("ids") List<String> ids);

    List<ChatRecord> selectAllByUserIdAndFriendId(@Param("userId") String userId, @Param("friendId") String friendId);

    int selectUserAddFriend(@Param("userId") String userId, @Param("friendId") String friendId);
}
