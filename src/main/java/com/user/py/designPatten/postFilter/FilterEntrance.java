package com.user.py.designPatten.postFilter;

import com.user.py.designPatten.postFilter.FilterBase.ArticleContext;
import com.user.py.designPatten.factory.ArticleFilterFactory;
import com.user.py.designPatten.postFilter.FilterBase.IArticleFilter;
import com.user.py.mode.request.AddPostRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author ice
 * @Date 2023/2/23 18:55
 * @Description: 过滤器
 */
public class FilterEntrance {
    private FilterEntrance() {
    }

    private static List<IArticleFilter> articleFilters;
    static {
        init();
    }
    private static void init() {
        articleFilters = ArticleFilterFactory.createArticleFilter();
    }

    public static boolean doFilter(AddPostRequest request) {
        ArticleContext articleContext = new ArticleContext();
        articleContext.setRequest(request);
        return doFilter(articleContext);
    }
    public static List<AddPostRequest> doFilter(List<AddPostRequest> requests) {
        return requests.stream().filter(FilterEntrance::doFilter).collect(Collectors.toList());
    }
    private  static boolean doFilter(ArticleContext articleContext) {
        for (IArticleFilter articleFilter : articleFilters) {
            if (!articleFilter.doFilter(articleContext)) {
                return false;
            }
        }
        return true;
    }
}
