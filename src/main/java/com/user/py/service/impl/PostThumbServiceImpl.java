package com.user.py.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.mapper.PostThumbMapper;
import com.user.py.mode.entity.PostThumb;
import com.user.py.service.IPostThumbService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 帖子点赞记录 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-02-14
 */
@Service
public class PostThumbServiceImpl extends ServiceImpl<PostThumbMapper, PostThumb> implements IPostThumbService {

}
