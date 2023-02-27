package com.user.py.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mapper.PostCommentMapper;
import com.user.py.mode.domain.Post;
import com.user.py.mode.domain.PostComment;
import com.user.py.mode.domain.User;
import com.user.py.mode.request.AddCommentRequest;
import com.user.py.service.IPostCommentService;
import com.user.py.service.IPostService;
import com.user.py.utils.SensitiveUtils;
import com.user.py.utils.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 评论表 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-02-15
 */
@Service
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment> implements IPostCommentService {
    @Resource
    private IPostService postService;

    @Override
    public boolean doComment(AddCommentRequest commentRequest, HttpServletRequest request) {
        if (commentRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        User loginUser = UserUtils.getLoginUser(request);
        String postId = commentRequest.getPostId();

        String userId = commentRequest.getUserId();
        String content = commentRequest.getContent();
        String replyId = commentRequest.getReplyId();
        if (!StringUtils.hasText(postId)) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        if (!StringUtils.hasText(content)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请填写内容!");
        }

        String loginUserId = loginUser.getId();
        if (!StringUtils.hasText(userId) || !loginUserId.equals(userId)) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        synchronized (loginUserId.intern()) {
            try {
                content = SensitiveUtils.sensitive(content);
            } catch (Exception e) {
                log.error("处理错误 "+e.getMessage());
            }
            Post post = postService.getById(postId);
            if (post == null) {
                throw new GlobalException(ErrorCode.NULL_ERROR, "该帖子已经被删除!");
            }
            QueryWrapper<PostComment> wrapper = new QueryWrapper<>();
            wrapper.eq("post_id", postId);
            wrapper.eq("user_id", userId);
            wrapper.eq("content", content);
            long count = this.count(wrapper);
            if (count > 2) {
                return false;
            }

            PostComment postComment = new PostComment();

            // 判断是回复，还是评论
            if (StringUtils.hasText(replyId)) {
                postComment.setReplyId(replyId);
            }
            postComment.setPostId(postId);
            postComment.setUserId(userId);

            postComment.setContent(content);
            return this.save(postComment);
        }


    }






}
