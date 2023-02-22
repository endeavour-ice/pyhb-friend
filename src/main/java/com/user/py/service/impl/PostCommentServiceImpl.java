package com.user.py.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mapper.PostCommentMapper;
import com.user.py.mode.domain.CommentReply;
import com.user.py.mode.domain.Post;
import com.user.py.mode.domain.PostComment;
import com.user.py.mode.domain.User;
import com.user.py.mode.domain.vo.CommentReplyVo;
import com.user.py.mode.domain.vo.PostCommentVo;
import com.user.py.mode.domain.vo.UserAvatarVo;
import com.user.py.mode.request.AddCommentRequest;
import com.user.py.mode.request.PostCommentRequest;
import com.user.py.service.ICommentReplyService;
import com.user.py.service.IPostCommentService;
import com.user.py.service.IPostService;
import com.user.py.service.IUserService;
import com.user.py.utils.UserUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Resource
    private ICommentReplyService replyService;
    @Resource
    private IUserService userService;

    @Override
    public boolean doComment(AddCommentRequest commentRequest, HttpServletRequest request) {
        if (commentRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        User loginUser = UserUtils.getLoginUser(request);
        String postId = commentRequest.getPostId();
        String commentId = commentRequest.getCommentId();
        String content = commentRequest.getContent();
        if (!StringUtils.hasText(postId)) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        if (!StringUtils.hasText(content)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请填写内容!");
        }
        Post post = postService.getById(postId);
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "该帖子已经被删除!");
        }
        String loginUserId = loginUser.getId();
        // 判断是回复，还是评论
        if (StringUtils.hasText(commentId)) {
            QueryWrapper<PostComment> postCommentQueryWrapper = new QueryWrapper<>();
            postCommentQueryWrapper.eq("post_id", postId);
            postCommentQueryWrapper.eq("id", commentId);
            long count = this.count(postCommentQueryWrapper);
            if (count <= 0) {
                throw new GlobalException(ErrorCode.NULL_ERROR,"该评论不存在!");
            }
            CommentReply commentReply = new CommentReply();
            commentReply.setPostId(postId);
            commentReply.setCommentId(commentId);
            commentReply.setUserId(loginUserId);
            commentReply.setReplyContent(content);
            return replyService.save(commentReply);
        } else {
            PostComment postComment = new PostComment();
            postComment.setPostId(postId);
            postComment.setUserId(loginUserId);
            postComment.setContent(content);
            return this.save(postComment);
        }

    }

    @Override
    public Page<PostCommentVo> getCommentList(PostCommentRequest commentRequest) {
        if (commentRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        String postId = commentRequest.getPostId();
        int pageNum = commentRequest.getPageNum();
        pageNum = pageNum <= 0 ? 1 : pageNum;
        int pageSize = commentRequest.getPageSize();
        pageSize = pageSize <= 0 || pageSize > 20 ? 10 : pageSize;
        if (!StringUtils.hasText(postId)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        Post post = postService.getById(postId);
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        List<String> userIds = new ArrayList<>();

        Page<PostComment> postCommentPage = new Page<>(pageNum, pageSize);
        QueryWrapper<PostComment> postCommentQueryWrapper = new QueryWrapper<>();
        postCommentQueryWrapper.eq("post_id", postId);
        postCommentPage = this.page(postCommentPage, postCommentQueryWrapper);
        Page<PostCommentVo> postCommentVoPage = new Page<>(postCommentPage.getCurrent(), postCommentPage.getSize(), postCommentPage.getTotal());
        List<PostCommentVo> commentVos = postCommentPage.getRecords().stream().map(comment -> {
            PostCommentVo postCommentVo = new PostCommentVo();
            postCommentVo.setId(comment.getId());
            postCommentVo.setUserId(comment.getUserId());
            postCommentVo.setContent(comment.getContent());
            userIds.add(comment.getUserId());
            return postCommentVo;
        }).collect(Collectors.toList());
        QueryWrapper<CommentReply> replyQueryWrapper = new QueryWrapper<>();
        postCommentQueryWrapper.eq("post_id", postId);
        List<CommentReply> replyList = replyService.list(replyQueryWrapper);
        Map<String, List<CommentReplyVo>> collect =
                replyList.stream().map(commentReply -> {
                    CommentReplyVo commentReplyVo = new CommentReplyVo();
                    commentReplyVo.setId(commentReply.getId());
                    commentReplyVo.setPostId(commentReply.getPostId());
                    commentReplyVo.setCommentId(commentReply.getCommentId());
                    commentReplyVo.setUserId(commentReply.getUserId());
                    commentReplyVo.setReplyContent(commentReply.getReplyContent());
                    return commentReplyVo;
                }).collect(Collectors.groupingBy(CommentReplyVo::getCommentId));
        Map<String, UserAvatarVo> avatarVoMap = userService.listByIds(userIds).stream().map(user -> {
            UserAvatarVo userAvatarVo = new UserAvatarVo();
            BeanUtils.copyProperties(user, userAvatarVo);
            return userAvatarVo;
        }).collect(Collectors.toMap(UserAvatarVo::getId, u -> u));
        ArrayList<CommentReplyVo> commentReplyVos = new ArrayList<>();
        for (PostCommentVo commentVo : commentVos) {
            commentVo.setUserVo(avatarVoMap.get(commentVo.getUserId()));
            List<CommentReplyVo> replyVoList = collect.get(commentVo.getId());
            commentVo.setCommentReplyVoList(replyVoList == null ? commentReplyVos : replyVoList);
        }
        postCommentVoPage.setRecords(commentVos);
        return postCommentVoPage;
    }
}
