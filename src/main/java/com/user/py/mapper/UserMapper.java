package com.user.py.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.user.py.mode.domain.User;
import org.apache.ibatis.annotations.Mapper;

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

}
