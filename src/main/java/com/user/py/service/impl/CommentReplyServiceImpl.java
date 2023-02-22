package com.user.py.service.impl;

import com.user.py.mode.domain.CommentReply;
import com.user.py.mapper.CommentReplyMapper;
import com.user.py.service.ICommentReplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 回复评论表 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-02-16
 */
@Service
public class CommentReplyServiceImpl extends ServiceImpl<CommentReplyMapper, CommentReply> implements ICommentReplyService {

}
