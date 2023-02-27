package com.user.py.designPatten.postFilter;

import com.user.py.designPatten.postFilter.FilterBase.ArticleContext;
import com.user.py.designPatten.postFilter.FilterBase.BaseArticleFilter;

/**
 * @Author ice
 * @Date 2023/2/23 18:45
 * @Description: TODO
 */
public class NumberOrderPostFilter extends BaseArticleFilter {
    @Override
    public boolean doFilter(ArticleContext articleContext) {
        if (articleContext.getRequest()!=null) {
            int length = articleContext.getRequest().getContent().length();
            return length >= 5 && length <= 200;
        }
        return true;
    }
}
