package com.user.py.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.mapper.UserNoticeMapper;
import com.user.py.mode.entity.UserNotice;
import com.user.py.service.IUserNoticeService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 公告表 服务实现类
 * </p>
 *
 * @author ice
 * @since 2022-09-18
 */
@Service
public class UserNoticeServiceImpl extends ServiceImpl<UserNoticeMapper, UserNotice> implements IUserNoticeService {

}
