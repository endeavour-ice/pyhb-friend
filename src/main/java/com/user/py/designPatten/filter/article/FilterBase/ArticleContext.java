package com.user.py.designPatten.filter.article.FilterBase;

import com.user.py.mode.request.AddCommentRequest;
import com.user.py.mode.request.AddPostRequest;
import lombok.Data;

/**
 * @Author ice
 * @Date 2023/2/23 18:03
 * @Description: TODO
 */
@Data
public class ArticleContext {
    private AddPostRequest request;
    private AddCommentRequest addCommentRequest;
}
