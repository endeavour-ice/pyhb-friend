package com.user.py.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.entity.Post;
import com.user.py.mode.entity.User;
import com.user.py.mode.entity.vo.PostVo;
import com.user.py.mode.request.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 帖子 服务类
 * </p>
 *
 * @author ice
 * @since 2023-02-13
 */
public interface IPostService extends IService<Post> {

    boolean addPost(AddPostRequest postRequest, User loginUser, MultipartFile file);

    Map<String,Object> getPostList(PostPageRequest postPageRequest, HttpServletRequest  request);

    boolean doThumb(String postId, HttpServletRequest request);

    boolean doCollect(String postId, HttpServletRequest request);
    boolean doComment(AddCommentRequest commentRequest, HttpServletRequest request);

    boolean delPost(String id, HttpServletRequest request);

    Map<String, String> getPostByCollection(HttpServletRequest request);

    PostVo getPost(String postId,User user);

    List<PostVo> getPostByCollect(HttpServletRequest request);

    List<PostVo> searchPost(SearchPostRequest searchPostRequest, User loginUser);

    /**
     * 删除评论
     * @param idRequest
     * @param request
     * @return
     */
    boolean delComment(DelCommRequest idRequest, HttpServletRequest request);
}
