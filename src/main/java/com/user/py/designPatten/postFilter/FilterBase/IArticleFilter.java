package com.user.py.designPatten.postFilter.FilterBase;

/**
 * @Author ice
 * @Date 2023/2/23 18:00
 * @Description: 过滤器模式+工厂模式
 */
public interface IArticleFilter {
    boolean doFilter(ArticleContext articleContext) throws Exception;
}
