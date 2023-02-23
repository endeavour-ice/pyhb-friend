package com.user.py.controller.PostController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.user.py.common.B;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mode.domain.User;
import com.user.py.mode.domain.vo.PostVo;
import com.user.py.mode.request.AddPostRequest;
import com.user.py.mode.request.PostDoThumbRequest;
import com.user.py.mode.request.PostPageRequest;
import com.user.py.service.IPostService;
import com.user.py.utils.UserUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 帖子 前端控制器
 * </p>
 *
 * @author ice
 * @since 2023-02-13
 */
@RestController
@RequestMapping("/post")
public class PostController {
    @Resource
    private IPostService postService;

    @PostMapping("/addPost")
    public B<Boolean> addPost(@RequestBody AddPostRequest postRequest, HttpServletRequest request) {
        if (postRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "数据为空");
        }
        User loginUser = UserUtils.getLoginUser(request);
        Boolean isSave=postService.addPost(postRequest, loginUser);
        return B.ok(isSave);
    }
    @PostMapping("/getPost")
    public B<Page<PostVo>> getPostList(HttpServletRequest request,@RequestBody PostPageRequest postPageRequest) {
        Page<PostVo> postList = postService.getPostList(postPageRequest, request);
        return B.ok(postList);
    }

    @GetMapping("/remove/{id}")
    public B<Boolean> removePost(@PathVariable String id, HttpServletRequest request) {
        if (!StringUtils.hasText(id)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = UserUtils.getLoginUser(request);
        boolean is =postService.removePostByID(id, loginUser);
        return B.ok(is);
    }

    /**
     * 帖子点赞
     * @param postDoThumbRequest
     * @param request
     * @return
     */
    @PostMapping("/doThumb")
    public B<Boolean> doThumb(@RequestBody PostDoThumbRequest postDoThumbRequest, HttpServletRequest request) {
        String postId;
        if (postDoThumbRequest == null || !StringUtils.hasText(postId = postDoThumbRequest.getPostId())) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        boolean isDo = postService.doThumb(postId, request);
        return isDo ? B.ok() : B.error(ErrorCode.ERROR);
    }

    // TODO 收藏的增删改查   这里要进行表字段的添加和创建

}
