package com.user.py.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.user.py.mode.entity.User;
import com.user.py.mode.entity.vo.UserAvatarVo;
import com.user.py.mode.entity.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author ice
 * @since 2022-06-14
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<UserAvatarVo> getUserAvatarVoByIds(String ids);

    long getUserCount();

    List<UserVo> selectFindByUserAccountLikePage(@Param("pageNum") long pageNum, @Param("pageSize")long pageSize, @Param("userName")String userName);


    UserVo selectByNameLike(@Param("name") String name);

    List<UserVo> selectUserVoList(@Param("current") long current, @Param("size") long size, @Param("status") int status);

    int saveFriend(@Param("id")String id,@Param("userId") String userId,@Param("friendId") String friendId);
}
