package com.user.py.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.user.py.common.ErrorCode;
import com.user.py.designPatten.singleton.GsonUtils;
import com.user.py.exception.GlobalException;
import com.user.py.mapper.PostMapper;
import com.user.py.mode.constant.CacheConstants;
import com.user.py.mode.dto.PageFilter;
import com.user.py.mode.entity.*;
import com.user.py.mode.entity.vo.CommentVo;
import com.user.py.mode.entity.vo.PostUserVo;
import com.user.py.mode.entity.vo.PostVo;
import com.user.py.mode.entity.vo.UserAvatarVo;
import com.user.py.mode.request.*;
import com.user.py.service.*;
import com.user.py.utils.RedisCache;
import com.user.py.utils.SensitiveUtils;
import com.user.py.utils.TimeUtils;
import com.user.py.utils.UserUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private IPostCommentService commentService;
    @Resource
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private ExecutorService executorService;
    @Resource
    private IPostCollectService collectService;

    /**
     * 添加论坛
     *
     * @param postRequest
     * @param loginUser
     * @param file
     * @return
     */
    @Override
    public boolean addPost(AddPostRequest postRequest, User loginUser, MultipartFile file) {
        // TODO 可以自定义标签，统计标签的数量，需要添加表或者字段
        if (postRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请填写数据");
        }
        String userId = loginUser.getId();
        synchronized (userId.intern()) {
            String tagId = postRequest.getTagId();
            String content = postRequest.getContent();
            String tag = null;
            if (StringUtils.hasText(tagId) && !"[]".equals(tagId)) {
                List<String> idList;
                try {
                    Gson gson = GsonUtils.getGson();
                    idList = gson.fromJson(tagId, new TypeToken<List<String>>() {
                    }.getType());
                    List<UserLabel> userLabels = labelService.listByIds(idList);
                    List<String> tagList;
                    if (!CollectionUtils.isEmpty(userLabels)) {
                        tagList = userLabels.stream().map(UserLabel::getLabel).collect(Collectors.toList());
                    } else {
                        tagList = new ArrayList<>();
                    }
                    tag = gson.toJson(tagList);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    throw new GlobalException(ErrorCode.PARAMS_ERROR, "解析出错");
                }
            }
            if (!StringUtils.hasText(content)) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "文章为空");
            }
            try {
                content = SensitiveUtils.sensitive(content);
            } catch (Exception e) {
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "过滤失败");
            }
            Post post = new Post();
            post.setUserId(userId);
            post.setContent(content);
            post.setTags(tag);
            post.setReviewStatus(1);
            post.setViewNum(0);
            post.setThumbNum(0);
            return this.save(post);
        }
    }


    @Override
    public Map<String, Object> getPostList(PostPageRequest postPageRequest, HttpServletRequest request) {
        if (postPageRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        // 过滤post的页面
        PageFilter pageFilter = new PageFilter(postPageRequest.getPageNum(), postPageRequest.getPageSize());
        long current = pageFilter.getCurrent();
        long size = pageFilter.getSize();

        Map<String, Object> map = new HashMap<>();
        String userId = null;
        if (postPageRequest.isOwn()) {
            User user = UserUtils.getLoginUser(request);
            userId = user.getId();
        }
        // 获取总数
        String finalUserId = userId;
        Integer total;
        CompletableFuture<Integer> integerCompletableFuture;
        integerCompletableFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectCountById(finalUserId), executorService);
        // 获取post列表和里面的用户名字头像id
        List<PostVo> postVoList = baseMapper.selectPostByUserOrderPage(current, size, postPageRequest.getSorted(), userId);

        postVoList = getPostVoList(postVoList, request);
        try {
            total = integerCompletableFuture.get();
            if (!postPageRequest.isOwn()) {
                redisCache.setCacheObject(CacheConstants.POST_TOTAL, total, TimeUtils.getRemainSecondsOneDay(new Date()), TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        if (postVoList.isEmpty()) {
            return map;
        }
        if (postPageRequest.getSorted() == 3 && postVoList.size() > 0) {
            postVoList = postVoList.stream().sorted((a, b) -> Integer.compare(b.getThumb(), a.getThumb())).collect(Collectors.toList());
        } else if (postPageRequest.getSorted() == 1 && postVoList.size() > 0) {
            postVoList = postVoList.stream().sorted((a, b) -> {
                LocalDateTime createTimeA = a.getCreateTime();
                LocalDateTime createTimeB = b.getCreateTime();
                return createTimeB.compareTo(createTimeA);
            }).collect(Collectors.toList());
        }
        map.put("current", current);
        map.put("size", size);
        map.put("records", postVoList);
        map.put("total", total);

        return map;
    }

    private List<PostVo> getPostVoList(List<PostVo> postVoList, HttpServletRequest request) {
        Map<String, List<PostVo>> postIdMap = postVoList.stream().collect(Collectors.groupingBy(PostVo::getId));
        Set<String> keySet = postIdMap.keySet();
        // 评论
        if (keySet.isEmpty()) {
            return new ArrayList<>();
        }
        CompletableFuture<Void> isThumb = null;
        // 是否点赞
        try {
            User loginUser = UserUtils.getLoginUser(request);
            isThumb = CompletableFuture.runAsync(() -> {
                String userId = loginUser.getId();
                QueryWrapper<PostCollect> postCollectQueryWrapper = new QueryWrapper<>();
                postCollectQueryWrapper.in("post_id", keySet);
                postCollectQueryWrapper.eq("user_id", userId);
                postCollectQueryWrapper.select("post_id");
                List<PostCollect> collectList = collectService.list(postCollectQueryWrapper);
                for (PostCollect postCollect : collectList) {
                    String collectPostId = postCollect.getPostId();
                    if (StringUtils.hasText(collectPostId)) {
                        List<PostVo> postVos = postIdMap.get(collectPostId);
                        if (!CollectionUtils.isEmpty(postVos)) {
                            PostVo postVo = postVos.get(0);
                            if (postVo != null) {
                                postVo.setHasCollect(true);
                            }
                        }
                    }
                }
                QueryWrapper<PostThumb> thumbQueryWrapper = new QueryWrapper<>();
                thumbQueryWrapper.in("post_id", keySet);
                thumbQueryWrapper.eq("user_id", userId);
                thumbQueryWrapper.select("post_id");
                List<PostThumb> thumbList = thumbService.list(thumbQueryWrapper);
                for (PostThumb postThumb : thumbList) {
                    String thumbPostId = postThumb.getPostId();
                    if (StringUtils.hasText(thumbPostId)) {
                        List<PostVo> postVos = postIdMap.get(thumbPostId);
                        if (!CollectionUtils.isEmpty(postVos)) {
                            PostVo postVo = postVos.get(0);
                            if (postVo != null) {
                                postVo.setHasThumb(true);
                            }
                        }
                    }
                }
            }, executorService);
        } catch (Exception e) {
            //无登录不做特殊处理
        }

        Set<String> userIdList = new HashSet<>();
        // 获取post的评论和用户
        List<CommentVo> postCommentByPostIds = baseMapper.getPostCommentByPostIds(keySet);
        for (CommentVo postCommentByPostId : postCommentByPostIds) {
            PostUserVo owner = postCommentByPostId.getOwner();
            if (owner == null) {
                continue;
            }
            String id = owner.getId();
            if (!StringUtils.hasText(id)) {
                continue;
            }
            userIdList.add(id);
        }
        for (String postId : keySet) {
            PostVo postVo = postIdMap.get(postId).get(0);
            if (postVo != null) {
                String userId = postVo.getPostUserVo().getId();
                userIdList.add(userId);
            }
        }
        Map<String, List<PostUserVo>> postUserVoListById = new HashMap<>();
        if (userIdList.size() > 0) {
            postUserVoListById = baseMapper.selectPostThumbTotal(userIdList).stream().collect(Collectors.groupingBy(UserAvatarVo::getId));
        }
        Map<String, List<CommentVo>> commentVoByPostIds = postCommentByPostIds.stream().collect(Collectors.groupingBy(CommentVo::getPostId));

        for (String postVoId : keySet) {
            PostVo postVo = postIdMap.get(postVoId).get(0);
            PostUserVo postUserVo = postVo.getPostUserVo();
            String id = postUserVo.getId();
            PostUserVo userVo = postUserVoListById.get(id).get(0);
            String postTotal = userVo.getPostTotal();
            String joinTime = userVo.getJoinTime();
            String thumbTotal = userVo.getThumbTotal();
            postUserVo.setPostTotal(postTotal);
            postUserVo.setJoinTime(joinTime);
            postUserVo.setThumbTotal(thumbTotal);
            List<CommentVo> commentVos = commentVoByPostIds.get(postVoId);
            if (!CollectionUtils.isEmpty(commentVos)) {
                for (CommentVo commentVo : commentVos) {
                    if (commentVo == null || commentVo.getOwner() == null) {
                        continue;
                    }
                    String userId = commentVo.getOwner().getId();
                    if (!StringUtils.hasText(userId)) {
                        continue;
                    }

                    List<PostUserVo> postUserVos = postUserVoListById.get(userId);
                    if (postUserVos==null||postUserVos.isEmpty() || postUserVos.get(0) == null) {
                        continue;
                    }
                    PostUserVo vo = postUserVos.get(0);
                    PostUserVo owner = commentVo.getOwner();
                    if (vo == null || owner == null) {
                        continue;
                    }
                    postTotal = vo.getPostTotal();
                    joinTime = vo.getJoinTime();
                    thumbTotal = vo.getThumbTotal();
                    owner.setPostTotal(postTotal);
                    owner.setJoinTime(joinTime);
                    owner.setThumbTotal(thumbTotal);
                }
            }
            postVo.setCommentList(commentVos);
        }
        if (isThumb != null) {
            try {
                isThumb.join();
            } catch (Exception e) {
                log.error("是否点赞多线程错误", e);
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
            }
        }

        return postIdMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
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
            TransactionStatus transaction = null;
            if (count >= 1) {
                // 取消点赞
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
                try {
                    transaction = dataSourceTransactionManager.getTransaction(transactionDefinition);
                    result = thumbService.save(postThumb);
                    if (result) {
                        result = this.update()
                                .eq("id", postId)
                                .setSql("thumb_num=thumb_num+1").update();
                    } else {
                        throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
                    }
                    dataSourceTransactionManager.commit(transaction);
                    return result;
                } catch (Exception e) {
                    if (transaction != null) {
                        dataSourceTransactionManager.rollback(transaction);
                    }
                    return false;
                }
            }
        }
    }

    @Override
    public boolean doCollect(String postId, HttpServletRequest request) {
        if (!StringUtils.hasText(postId)) {
            return false;
        }
        User loginUser = UserUtils.getLoginUser(request);
        Post post = this.getById(postId);
        if (post == null) {
            return false;
        }
        String userId = loginUser.getId();
        boolean result;
        synchronized (userId.intern()) {
            QueryWrapper<PostCollect> thumbQueryWrapper = new QueryWrapper<>();
            thumbQueryWrapper.eq("post_id", postId);
            thumbQueryWrapper.eq("user_id", userId);
            long count = collectService.count(thumbQueryWrapper);
            TransactionStatus transaction = null;
            if (count >= 1) {
                // 取消
                try {
                    transaction = dataSourceTransactionManager.getTransaction(transactionDefinition);
                    result = collectService.remove(thumbQueryWrapper);
                    if (!result) {
                        throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
                    }
                    result = this.update()
                            .eq("id", postId)
                            .ge("collect_num", 0)
                            .setSql("collect_num=collect_num-1").update();
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
                // 收藏
                PostCollect postCollect = new PostCollect();
                postCollect.setPostId(postId);
                postCollect.setUserId(userId);
                try {
                    transaction = dataSourceTransactionManager.getTransaction(transactionDefinition);
                    result = collectService.save(postCollect);
                    if (result) {
                        result = this.update()
                                .eq("id", postId)
                                .setSql("collect_num=collect_num+1").update();
                    } else {
                        throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
                    }
                    dataSourceTransactionManager.commit(transaction);
                    return result;
                } catch (Exception e) {
                    if (transaction != null) {
                        dataSourceTransactionManager.rollback(transaction);
                    }
                    return false;
                }

            }
        }
    }

    @Override
    public boolean doComment(AddCommentRequest commentRequest, HttpServletRequest request) {
        if (commentRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        User loginUser = UserUtils.getLoginUser(request);
        String postId = commentRequest.getPostId();
        String userId = commentRequest.getUserId();
        String content = commentRequest.getContent();
        if (!StringUtils.hasText(postId)) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        if (!StringUtils.hasText(content)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请填写内容!");
        }

        String loginUserId = loginUser.getId();
        if (!StringUtils.hasText(userId) || !loginUserId.equals(userId)) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        synchronized (loginUserId.intern()) {
            try {
                content = SensitiveUtils.sensitive(content);
            } catch (Exception e) {
                log.error("处理错误 " + e.getMessage());
            }
            Post post = this.getById(postId);
            if (post == null) {
                throw new GlobalException(ErrorCode.NULL_ERROR, "该帖子已经被删除!");
            }
            QueryWrapper<PostComment> wrapper = new QueryWrapper<>();
            wrapper.eq("post_id", postId);
            wrapper.eq("user_id", userId);
            wrapper.eq("content", content);
            long count = commentService.count(wrapper);
            if (count > 2) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "评论重复");
            }
            PostComment postComment = new PostComment();
            postComment.setPostId(postId);
            postComment.setUserId(userId);
            postComment.setContent(content);
            return commentService.save(postComment);
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delPost(String id, HttpServletRequest request) {
        if (!StringUtils.hasText(id) || request == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = UserUtils.getLoginUser(request);
        Post post = this.getById(id);
        if (post == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        String loginUserId = loginUser.getId();
        String userId = post.getUserId();
        if (!loginUserId.equals(userId) && !UserUtils.isAdmin(loginUser)) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<PostThumb> thumbQueryWrapper = new QueryWrapper<>();
        thumbQueryWrapper.eq("post_id", id);
        thumbService.remove(thumbQueryWrapper);
        QueryWrapper<PostCollect> postCollectQueryWrapper = new QueryWrapper<>();
        postCollectQueryWrapper.eq("post_id", id);
        collectService.remove(postCollectQueryWrapper);
        QueryWrapper<PostComment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq("post_id", id);
        commentService.remove(commentQueryWrapper);
        return this.removeById(id);
    }

    /**
     * 获取收藏数
     *
     * @param request
     * @return
     */
    @Override
    public Map<String, String> getPostByCollection(HttpServletRequest request) {
        User user = UserUtils.getLoginUser(request);
        String id = user.getId();
        Map<String, String> map = new HashMap<>(3);
        List<PostUserVo> postUserVos = baseMapper.selectPostThumbTotal(Collections.singletonList(id));
        if (postUserVos.isEmpty()) {
            return null;
        }
        PostUserVo postUserVo = postUserVos.get(0);
        String totalPost = postUserVo.getPostTotal();
        String totalThumb = postUserVo.getThumbTotal();
        QueryWrapper<PostCollect> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", id);
        long count = collectService.count(wrapper);
        String totalCollect = String.valueOf(count);
        map.put("totalPost", totalPost);
        map.put("totalThumb", totalThumb);
        map.put("totalCollect", totalCollect);
        return map;
    }

    @Override
    public PostVo getPost(String postId, User loginUser) {
        String userId = loginUser.getId();
        PostVo postVo = baseMapper.selectPostUserOrderById(postId);
        if (postVo == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        List<CommentVo> commentVoList = baseMapper.getPostCommentByPostIds(Collections.singletonList(postId));
        for (CommentVo commentVo : commentVoList) {
            String userID = commentVo.getOwner().getId();
            if (userID.equals(userId)) {
                commentVo.set_com(true);
            }
        }
        postVo.setCommentList(commentVoList);
        QueryWrapper<PostCollect> postCollectQueryWrapper = new QueryWrapper<>();
        postCollectQueryWrapper.eq("post_id", postId);
        postCollectQueryWrapper.eq("user_id", userId);
        long countCollect = collectService.count(postCollectQueryWrapper);
        if (countCollect == 1) {
            postVo.setHasCollect(true);
        }
        QueryWrapper<PostThumb> thumbQueryWrapper = new QueryWrapper<>();
        thumbQueryWrapper.eq("post_id", postId);
        thumbQueryWrapper.eq("user_id", userId);

        long countThumb = thumbService.count(thumbQueryWrapper);
        if (countThumb == 1) {
            postVo.setHasThumb(true);
        }
        this.update()
                .eq("id", postVo.getId())
                .setSql("view_num=view_num+1").update();
        return postVo;
    }

    /**
     * 根据收藏数获取文章
     *
     * @param request
     * @return
     */
    @Override
    public List<PostVo> getPostByCollect(HttpServletRequest request) {
        User loginUser = UserUtils.getLoginUser(request);
        String userId = loginUser.getId();
        return baseMapper.selectPostCollectByUserId(userId);
    }

    @Override
    public List<PostVo> searchPost(SearchPostRequest searchPostRequest, User loginUser) {
        if (searchPostRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        String content = searchPostRequest.getContent();
        String userId = searchPostRequest.getUserId();
        List<PostVo> postVoList = null;
        if (StringUtils.hasText(content)) {
            postVoList = baseMapper.searchContent(content);
            return postVoList;
        } else if (StringUtils.hasText(userId)) {
            postVoList = baseMapper.searchUser(userId);
        }

        return postVoList;
    }

    @Override
    public boolean delComment(DelCommRequest commRequest, HttpServletRequest request) {
        if (commRequest==null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User user = UserUtils.getLoginUser(request);
        String userId = user.getId();
        synchronized (userId.intern()) {
            String id = commRequest.getId();
            String postId = commRequest.getPostId();
            if (!StringUtils.hasText(id)||!StringUtils.hasText(postId)) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR);
            }
            PostComment comment = commentService.getById(id);
            if (comment == null) {
                throw new GlobalException(ErrorCode.NULL_ERROR);
            }
            String commentUserId = comment.getUserId();
            String commentPostId = comment.getPostId();
            if (!StringUtils.hasText(commentUserId)||!StringUtils.hasText(commentPostId)) {
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
            }
            if (!userId.equals(commentUserId)||!postId.equals(commentPostId)) {
                throw new GlobalException(ErrorCode.NO_AUTH);
            }
            return commentService.removeById(comment.getId());
        }

    }
}
