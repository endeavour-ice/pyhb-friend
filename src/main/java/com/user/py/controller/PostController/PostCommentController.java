package com.user.py.controller.PostController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.user.py.common.B;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mode.domain.vo.PostCommentVo;
import com.user.py.mode.request.AddCommentRequest;
import com.user.py.mode.request.PostCommentRequest;
import com.user.py.service.IPostCommentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 评论表 前端控制器
 * </p>
 *
 * @author ice
 * @since 2023-02-15
 */
@RestController
@RequestMapping("/postComment")
public class PostCommentController {
    @Resource
    private IPostCommentService commentService;

    @PostMapping("/doComment")
    public B<Boolean> doComment(@RequestBody AddCommentRequest commentRequest, HttpServletRequest request) {
        if (commentRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        boolean isCom= commentService.doComment(commentRequest, request);
        return isCom ? B.ok() : B.error(ErrorCode.ERROR);
    }

    @PostMapping("/getComment")
    public B<Page<PostCommentVo>> getCommentList(@RequestBody PostCommentRequest commentRequest, HttpServletRequest request) {
        Page<PostCommentVo> postCommentVos = commentService.getCommentList(commentRequest);
        return B.ok(postCommentVos);
    }
}
