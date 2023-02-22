package com.user.py.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.user.py.mode.domain.Post;
import com.user.py.mode.domain.User;
import com.user.py.mode.domain.vo.PostVo;
import com.user.py.mode.request.AddPostRequest;
import com.user.py.mode.request.PostPageRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 帖子 服务类
 * </p>
 *
 * @author ice
 * @since 2023-02-13
 */
public interface IPostService extends IService<Post> {

    boolean addPost(AddPostRequest postRequest, User loginUser);

    Page<PostVo> getPostList(PostPageRequest postPageRequest, HttpServletRequest  request);

    boolean removePostByID(String id, User loginUser);

    boolean doThumb(String postId, HttpServletRequest request);


}
