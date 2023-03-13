package com.user.py.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.mapper.PostCollectMapper;
import com.user.py.mode.entity.PostCollect;
import com.user.py.service.IPostCollectService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 帖子收藏记录 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-03-10
 */
@Service
public class PostCollectServiceImpl extends ServiceImpl<PostCollectMapper, PostCollect> implements IPostCollectService {

}
