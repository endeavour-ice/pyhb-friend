package com.user.py.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.domain.PostComment;
import com.user.py.mode.request.AddCommentRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 评论表 服务类
 * </p>
 *
 * @author ice
 * @since 2023-02-15
 */
public interface IPostCommentService extends IService<PostComment> {

    boolean doComment(AddCommentRequest commentRequest, HttpServletRequest request);



}
