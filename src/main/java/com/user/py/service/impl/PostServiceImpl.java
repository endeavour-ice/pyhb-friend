package com.user.py.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.common.ErrorCode;
import com.user.py.designPatten.postFilter.FilterEntrance;
import com.user.py.exception.GlobalException;
import com.user.py.mapper.PostMapper;
import com.user.py.mode.constant.PostSortedType;
import com.user.py.mode.domain.Post;
import com.user.py.mode.domain.PostThumb;
import com.user.py.mode.domain.User;
import com.user.py.mode.domain.UserLabel;
import com.user.py.mode.domain.vo.CommentVo;
import com.user.py.mode.domain.vo.PostVo;
import com.user.py.mode.domain.vo.UserAvatarVo;
import com.user.py.mode.request.AddPostRequest;
import com.user.py.mode.request.PostPageRequest;
import com.user.py.service.IPostService;
import com.user.py.service.IPostThumbService;
import com.user.py.service.IUserLabelService;
import com.user.py.service.IUserService;
import com.user.py.utils.RedisCache;
import com.user.py.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.user.py.mode.constant.RedisKey.redisPostList;

/**
 * <p>
 * 帖子 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-02-13
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    @Resource
    private IPostThumbService thumbService;
    @Resource
    private IUserLabelService labelService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private IUserService userService;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;

    /**
     * 添加论坛
     *
     * @param postRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean addPost(AddPostRequest postRequest, User loginUser) {
        // TODO 可以自定义标签，统计标签的数量，需要添加表或者字段
        if (postRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请填写数据");
        }
        String content = postRequest.getContent();
        String tagId = postRequest.getTagId();
        String userId = postRequest.getUserId();
        String id = loginUser.getId();

        // 防止重复点击
        if (!StringUtils.hasText(id)) {
            throw new GlobalException(ErrorCode.NO_LOGIN);
        }
        synchronized (id.intern()) {
            if (!StringUtils.hasText(content) || !StringUtils.hasText(tagId)) {
                throw new GlobalException(ErrorCode.NULL_ERROR);
            }
            try {
                if (!FilterEntrance.doFilter(postRequest,null)) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR, "文章内容不规范");
                }
            } catch (Exception ignored) {

            }
            if (!StringUtils.hasText(userId) || !StringUtils.hasText(id)) {
                throw new GlobalException(ErrorCode.NO_LOGIN);
            }
            if (!userId.equals(id)) {
                throw new GlobalException(ErrorCode.NO_LOGIN);
            }

            Post post = new Post();
            post.setUserId(userId);
            post.setContent(content);
            post.setTagId(tagId);
            boolean save = this.save(post);
            if (save) {
                redisCache.removeLikeKey(redisPostList);
            }
            return save;
        }
    }

    @Override
    public Page<PostVo> getPostList(PostPageRequest postPageRequest, HttpServletRequest request) {

        if (postPageRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        String userId = postPageRequest.getUserId();
        String content = postPageRequest.getContent();
        String tagId = postPageRequest.getTagId();
        Integer sorted = postPageRequest.getSorted();
        int pageNum = postPageRequest.getPageNum();
        int current = pageNum <= 0 ? 1 : pageNum;
        int size = postPageRequest.getPageSize();
        int pageSize = size <= 0 ? 10 : size;
        if (pageSize > 30) {
            pageSize = 20;
        }
        postQueryWrapper.eq(StringUtils.hasText(userId), "user_id", userId);
        postQueryWrapper.like(StringUtils.hasText(content), "content", content);
        postQueryWrapper.eq(StringUtils.hasText(tagId), "tag_id", tagId);
        if (sorted != null) {
            switch (sorted) {
                case (PostSortedType.SORT_ORDER_DESC):
                    postQueryWrapper.orderByDesc("create_time");
                    break;
                case (PostSortedType.SORT_THUMBS_MOST):
                    postQueryWrapper.orderByAsc("thumb_num");
                    break;
            }
        }
        Page<Post> postPage = baseMapper.selectPage(new Page<>(current, pageSize), postQueryWrapper);
        Page<PostVo> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        List<Post> postList = postPage.getRecords();
        if (postList.size() <= 0) {
            return postVOPage;
        }
        List<String> userIds = new ArrayList<>();
        // 获取标签
        Map<String, UserLabel> labelMap = getLabelMap();
        for (Post post : postList) {
            userIds.add(post.getUserId());
        }
        Map<String, List<UserAvatarVo>> userAvatarVoMap = userService.getUserAvatarVoByIds(userIds).stream().collect(Collectors.groupingBy(UserAvatarVo::getId));
        Map<String, List<PostVo>> postIdListMap = postList.stream().map(post -> {
            PostVo postVo = new PostVo();
            postVo.setId(post.getId());
            postVo.setContent(post.getContent());
            postVo.setThumb(post.getThumbNum());
            if (labelMap != null && labelMap.size() > 0) {
                String postTagId = post.getTagId();
                UserLabel userLabel = labelMap.get(postTagId);
                String label = null;
                if (userLabel != null) {
                     label = userLabel.getLabel();
                }
                postVo.setTag(label);
            }
            postVo.setUserAvatarVo(userAvatarVoMap.get(post.getUserId()).get(0));
            postVo.setHasThumb(false);
            return postVo;
        }).collect(Collectors.groupingBy(PostVo::getId));
        Set<String> postIds = postIdListMap.keySet();
        CompletableFuture<Map<String, List<String>>> completableFuture =
                CompletableFuture.supplyAsync(() -> getCommentByListPostId(postIds));

        // 是否点赞

        try {
            User loginUser = UserUtils.getLoginUser(request);
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("post_id", postIdListMap.keySet());
            postThumbQueryWrapper.eq("user_id", loginUser.getId());
            List<PostThumb> postThumbList = thumbService.list(postThumbQueryWrapper);
            postThumbList.forEach(postThumb -> postIdListMap.get(postThumb.getPostId()).get(0).setHasThumb(true));
        } catch (Exception e) {
            //无登录不做特殊处理
        }
        // 评论
        List<PostVo> postVoList = postIdListMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        Map<String, List<String>> map;
        try {
            map = completableFuture.get();
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
        for (PostVo postVo : postVoList) {
            String id = postVo.getId();
            postVo.setCommentList(map.get(id));
        }
        // TODO 是否收藏
        postVOPage.setRecords(postVoList);
        return postVOPage;
    }


    /**
     * 获取标签
     *
     * @return
     */
    private Map<String, UserLabel> getLabelMap() {
        List<UserLabel> labelList = labelService.list();
        if (labelList != null && labelList.size() > 0) {
            return labelList.stream().collect(Collectors.toMap(UserLabel::getId, userLabel -> userLabel));
        }
        return null;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removePostByID(String id, User loginUser) {
        if (!StringUtils.hasText(id)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        Post post = baseMapper.selectById(id);
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "数据不存在");
        }
        String userId = loginUser.getId();
        boolean admin = UserUtils.isAdmin(loginUser);
        if (!id.equals(userId) && !admin) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        int deleteById = baseMapper.deleteById(id);
        if (deleteById <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<PostThumb> thumbQueryWrapper = new QueryWrapper<>();
        thumbQueryWrapper.eq("post_id", id);
        if (thumbService.count(thumbQueryWrapper) > 0) {
            if (!thumbService.removeById(thumbQueryWrapper)) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR);
            }
        }
        return true;
    }

    private Map<String, List<String>> getCommentByListPostId(Set<String> postIds) {
        Map<String, List<String>> hashMap = new HashMap<>();
        if (CollectionUtils.isEmpty(postIds)) {
            return hashMap;
        }
        List<CommentVo> postCommentByPostIds = baseMapper.getPostCommentByPostIds(postIds);
        Map<String, List<CommentVo>> map = postCommentByPostIds.stream().collect(Collectors.groupingBy(CommentVo::getPostId));
        Set<String> pIds = map.keySet();
        for (String pId : pIds) {
            List<String> commentStringList = new ArrayList<>();
            List<CommentVo> commentVoList = map.get(pId);
            for (CommentVo commentVo : commentVoList) {
                String commentName = commentVo.getCommentName();
                String replyName = commentVo.getReplyName();
                String content = commentVo.getContent();
                String format;
                if (StringUtils.hasText(replyName)) {
                    format = String.format("%s 回复 %s: %s", commentName, replyName, content);
                } else {
                    format = String.format("%s: %s", commentName, content);
                }
                commentStringList.add(format);
            }
            hashMap.put(pId, commentStringList);
        }
        return hashMap;
    }

    @Override
    public boolean doThumb(String postId, HttpServletRequest request) {
        if (!StringUtils.hasText(postId)) {
            return false;
        }
        final User loginUser = UserUtils.getLoginUser(request);
        Post post = this.getById(postId);
        if (post == null) {
            return false;
        }
        String userId = loginUser.getId();
        boolean result;
        synchronized (userId.intern()) {
            QueryWrapper<PostThumb> thumbQueryWrapper = new QueryWrapper<>();
            thumbQueryWrapper.eq("post_id", postId);
            thumbQueryWrapper.eq("user_id", userId);
            long count = thumbService.count(thumbQueryWrapper);
            if (count >= 1) {
                // 取消点赞
                TransactionStatus transaction = null;
                try {
                    transaction = dataSourceTransactionManager.getTransaction(transactionDefinition);
                    result = thumbService.remove(thumbQueryWrapper);
                    if (!result) {
                        throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
                    }
                    result = this.update()
                            .eq("id", postId)
                            .ge("thumb_num", 0)
                            .setSql("thumb_num=thumb_num-1").update();
                    if (!result) {
                        throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
                    }
                    dataSourceTransactionManager.commit(transaction);
                    return true;
                } catch (Exception e) {
                    if (transaction != null) {
                        dataSourceTransactionManager.rollback(transaction);
                    }
                    return false;
                }

            } else {
                // 点赞
                PostThumb postThumb = new PostThumb();
                postThumb.setPostId(postId);
                postThumb.setUserId(userId);
                result = thumbService.save(postThumb);
                if (result) {
                    result = this.update()
                            .eq("id", postId)
                            .setSql("thumb_num=thumb_num+1").update();
                    return result;

                } else {
                    throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
                }
            }
        }
    }


}
