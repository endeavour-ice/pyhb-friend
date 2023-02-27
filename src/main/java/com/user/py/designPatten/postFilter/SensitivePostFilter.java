package com.user.py.designPatten.postFilter;

import com.user.py.designPatten.postFilter.FilterBase.ArticleContext;
import com.user.py.designPatten.postFilter.FilterBase.BaseArticleFilter;
import com.user.py.mode.request.AddCommentRequest;
import com.user.py.mode.request.AddPostRequest;
import com.user.py.utils.SensitiveUtils;

/**
 * @Author ice
 * @Date 2023/2/23 18:10
 * @Description: TODO
 */
public class SensitivePostFilter extends BaseArticleFilter {
    @Override
    public boolean doFilter(ArticleContext articleContext) throws Exception {

        AddPostRequest request = articleContext.getRequest();
        AddCommentRequest addCommentRequest = articleContext.getAddCommentRequest();
        if (request != null) {
            String content = request.getContent();
            request.setContent(SensitiveUtils.sensitive(content));
        }
        if (addCommentRequest != null) {
            String content = addCommentRequest.getContent();
            addCommentRequest.setContent(SensitiveUtils.sensitive(content));
        }
        return true;
    }
}
