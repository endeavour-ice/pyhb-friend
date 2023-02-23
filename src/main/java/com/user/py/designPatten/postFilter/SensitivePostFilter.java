package com.user.py.designPatten.postFilter;

import com.user.py.designPatten.postFilter.FilterBase.ArticleContext;
import com.user.py.designPatten.postFilter.FilterBase.BaseArticleFilter;
import com.user.py.mode.request.AddPostRequest;

/**
 * @Author ice
 * @Date 2023/2/23 18:10
 * @Description: TODO
 */
public class SensitivePostFilter extends BaseArticleFilter {
    @Override
    public boolean doFilter(ArticleContext articleContext) {
        AddPostRequest request = articleContext.getRequest();
        String content = request.getContent();
        System.out.println("字数少于10");
        return true;
    }
}
