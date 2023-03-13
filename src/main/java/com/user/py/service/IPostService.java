package com.user.py.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.entity.Post;
import com.user.py.mode.entity.User;
import com.user.py.mode.entity.vo.PostVo;
import com.user.py.mode.request.AddCommentRequest;
import com.user.py.mode.request.AddPostRequest;
import com.user.py.mode.request.PostPageRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
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

    Page<PostVo> getPostList(PostPageRequest postPageRequest, HttpServletRequest  request);

    boolean removePostByID(String id, User loginUser);

    boolean doThumb(String postId, HttpServletRequest request);


    boolean doCollect(String postId, HttpServletRequest request);
    boolean doComment(AddCommentRequest commentRequest, HttpServletRequest request);
    Map<String, Object> getPostListByUser(PostPageRequest postPageRequest, HttpServletRequest request);

    boolean delPost(String id, HttpServletRequest request);
}
