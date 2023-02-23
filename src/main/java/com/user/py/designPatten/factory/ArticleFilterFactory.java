package com.user.py.designPatten.factory;

import com.user.py.designPatten.postFilter.FilterBase.ArticleFilterInterFace;
import com.user.py.designPatten.postFilter.FilterBase.IArticleFilter;
import com.user.py.designPatten.postFilter.NumberOrderPostFilter;
import com.user.py.designPatten.postFilter.SensitivePostFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author ice
 * @Date 2023/2/23 18:16
 * @Description: TODO
 */
public class ArticleFilterFactory {
    private static final Map<Integer, IArticleFilter> articleFilterMap = new HashMap<Integer, IArticleFilter>() {
        private static final long serialVersionUID = -8111296656606366039L;
        {
            put(ArticleFilterInterFace.NUMBER, new NumberOrderPostFilter());
            put(ArticleFilterInterFace.SENSITIVE, new SensitivePostFilter());
        }
    };

    public static List<IArticleFilter> createArticleFilter() {
        List<IArticleFilter> articleFilters = null;
        if (!articleFilterMap.isEmpty()) {
            articleFilters = new ArrayList<>(articleFilterMap.values());
        }
        return articleFilters;
    }

}
