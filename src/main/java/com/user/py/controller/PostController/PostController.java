package com.user.py.controller.PostController;

import com.user.py.annotation.AuthSecurity;
import com.user.py.annotation.CurrentLimiting;
import com.user.py.common.B;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mode.entity.User;
import com.user.py.mode.entity.vo.PostVo;
import com.user.py.mode.enums.UserRole;
import com.user.py.mode.request.*;
import com.user.py.service.IPostService;
import com.user.py.utils.UserUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

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
    public B<Boolean> addPost(@RequestBody AddPostRequest postRequest, MultipartFile file, HttpServletRequest request) {

        if (postRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "数据为空");
        }
        User loginUser = UserUtils.getLoginUser(request);
        Boolean isSave = postService.addPost(postRequest, loginUser, file);
        return B.ok(isSave);
    }

    @PostMapping("/getPost")
    public B<Map<String,Object>> getPostList(HttpServletRequest request, @RequestBody PostPageRequest postPageRequest) {
        Map<String,Object> postList = postService.getPostList(postPageRequest, request);
        return B.ok(postList);
    }
    @GetMapping ("/get")
    public B<PostVo> getPostById(HttpServletRequest request,@RequestParam String postId) {
        User loginUser = UserUtils.getLoginUser(request);
        if (!StringUtils.hasText(postId)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        PostVo post = postService.getPost(postId,loginUser);
        return B.ok(post);
    }

    @GetMapping("/col")
    public B<Map<String, String>> getPostByCollection(HttpServletRequest request) {
        Map<String,String> map= postService.getPostByCollection(request);
        return B.ok(map);
    }
    /**
     * 帖子点赞
     */
    @PostMapping("/doThumb")
    @CurrentLimiting
    public B<Boolean> doThumb(@RequestBody PostDoThumbRequest postDoThumbRequest, HttpServletRequest request) {
        String postId;
        if (postDoThumbRequest == null || !StringUtils.hasText(postId = postDoThumbRequest.getPostId())) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        boolean isDo = postService.doThumb(postId, request);
        return isDo ? B.ok() : B.error(ErrorCode.ERROR);
    }

    /**
     * 帖子收藏
     *
     * @param postDoThumbRequest
     * @param request
     * @return
     */
    @PostMapping("/doCollect")
    @CurrentLimiting
    public B<Boolean> doCollect(@RequestBody PostDoThumbRequest postDoThumbRequest, HttpServletRequest request) {
        String postId;
        if (postDoThumbRequest == null || !StringUtils.hasText(postId = postDoThumbRequest.getPostId())) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        boolean isDo = postService.doCollect(postId, request);
        return isDo ? B.ok() : B.error(ErrorCode.ERROR);
    }

    @PostMapping("/doComment")
    public B<Boolean> doComment(@RequestBody AddCommentRequest commentRequest, HttpServletRequest request) {
        if (commentRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        boolean isCom = postService.doComment(commentRequest, request);
        return isCom ? B.ok() : B.error(ErrorCode.ERROR);
    }

    @PostMapping("/delPost")
    @AuthSecurity(isNoRole = {UserRole.TEST})
    public B<Boolean> delPost(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        String id = idRequest.getId();
        boolean isPost = postService.delPost(id, request);
        return B.ok(isPost);
    }
    @GetMapping("/collect")
    public B<List<PostVo>> getPostByCollect(HttpServletRequest request) {
        List<PostVo> isPost = postService.getPostByCollect(request);
        return B.ok(isPost);
    }
    @PostMapping("/search")
    public B<List<PostVo>> searchPost(@RequestBody SearchPostRequest searchPostRequest,HttpServletRequest request) {
        if (searchPostRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = UserUtils.getLoginUser(request);
        List<PostVo> postVoList = postService.searchPost(searchPostRequest, loginUser);
        return B.ok(postVoList);
    }

    /**
     * 删除评论
     * @param commRequest
     * @return
     */
    @PostMapping("/del")
    public B<Boolean> delComment(@RequestBody DelCommRequest commRequest,HttpServletRequest request) {
        if (commRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
       boolean is= postService.delComment(commRequest, request);
        return B.ok(is);
    }
}
