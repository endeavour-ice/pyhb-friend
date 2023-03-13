package com.user.py.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.user.py.common.ErrorCode;
import com.user.py.designPatten.singleton.GsonUtils;
import com.user.py.exception.GlobalException;
import com.user.py.mapper.PostMapper;
import com.user.py.mode.constant.RedisKey;
import com.user.py.mode.entity.*;
import com.user.py.mode.entity.vo.CollectThumbVo;
import com.user.py.mode.entity.vo.CommentVo;
import com.user.py.mode.entity.vo.PostVo;
import com.user.py.mode.entity.vo.UserAvatarVo;
import com.user.py.mode.request.AddCommentRequest;
import com.user.py.mode.request.AddPostRequest;
import com.user.py.mode.request.PostPageRequest;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
    private IPostCommentService  commentService;
    @Resource
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;

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
                    throw new GlobalException(ErrorCode.PARAMS_ERROR);
                }
            }
            if (!StringUtils.hasText(content)) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "文章为空");
            }
            try {
                content = SensitiveUtils.sensitive(content);
            } catch (Exception e) {
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
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
    public Map<String, Object> getPostListByUser(PostPageRequest postPageRequest, HttpServletRequest request) {
        if (postPageRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        AtomicInteger totalThumb = new AtomicInteger();
        Integer sorted = postPageRequest.getSorted();
        User loginUser = UserUtils.getLoginUser(request);
        String userId = loginUser.getId();
        String userAccount = loginUser.getUserAccount();
        String avatarUrl = loginUser.getAvatarUrl();
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.select("id", "content", "tags", "collect_num", "thumb_num");
        wrapper.eq("user_id", userId);
        List<Post> list = this.list(wrapper);
        List<PostVo> postVoList = list.stream().map(post -> {
            PostVo postVo = new PostVo();
            postVo.setId(post.getId());
            postVo.setContent(post.getContent());
            Integer thumbNum = post.getThumbNum();
            Integer collectNum = post.getCollectNum();
            totalThumb.addAndGet(thumbNum);
            postVo.setThumb(thumbNum);
            postVo.setCollect(collectNum);
            postVo.setTag(post.getTags());
            UserAvatarVo userAvatarVo = new UserAvatarVo();
            userAvatarVo.setId(userId);
            userAvatarVo.setUsername(userAccount);
            userAvatarVo.setAvatarUrl(avatarUrl);
            postVo.setUserAvatarVo(userAvatarVo);
            return postVo;
        }).collect(Collectors.toList());
        postVoList = getPostVoList(postVoList, request);
        QueryWrapper<PostCollect> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        long totalCollect = collectService.count(queryWrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("totalThumb", totalThumb);
        map.put("totalCollect", totalCollect);
        map.put("totalPost", postVoList.size());
        if (sorted == 1) {
        } else if (sorted == 3) {
            postVoList = postVoList.stream().sorted((a, b) -> Integer.compare(b.getThumb(), a.getThumb())).collect(Collectors.toList());
        } else {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        map.put("postList", postVoList);
        return map;
    }

    @Override
    public Page<PostVo> getPostList(PostPageRequest postPageRequest, HttpServletRequest request) {
        if (postPageRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        long pageNum = postPageRequest.getPageNum();
        long current = pageNum <= 0 ? 1 : pageNum;
        long size = postPageRequest.getPageSize();
        long pageSize = size <= 0 ? 10 : size;
        if (pageSize > 30) {
            pageSize = 20;
        }
        Page<PostVo> postVoPage = null;
        CompletableFuture<Integer> integerCompletableFuture = null;
        if (redisCache.hasKey(RedisKey.postTotal)) {
            Integer total = redisCache.getCacheObject(RedisKey.postTotal);
            postVoPage = new Page<>(current, pageSize, total);
        } else {
            integerCompletableFuture = CompletableFuture.supplyAsync(() -> {
                int count = baseMapper.selectCountById();
                redisCache.setCacheObject(RedisKey.postTotal, count, TimeUtils.getRemainSecondsOneDay(new Date()), TimeUnit.SECONDS);
                return count;
            });
        }
        long currentNum = (current - 1) * pageSize;
        List<PostVo> postVoList = baseMapper.selectIndexByPage(currentNum, pageSize,postPageRequest.getSorted());

        postVoList = getPostVoList(postVoList, request);
        if (postVoPage == null) {
            try {
                Integer total = integerCompletableFuture.get();
                postVoPage = new Page<>(current, pageSize, total);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        } else {
            postVoPage.setRecords(postVoList);
        }
        return postVoPage;
    }

    private List<PostVo> getPostVoList(List<PostVo> postVoList, HttpServletRequest request) {
        Map<String, List<PostVo>> postIdMap = postVoList.stream().collect(Collectors.groupingBy(PostVo::getId));
        Set<String> keySet = postIdMap.keySet();
        // 评论
        CompletableFuture<Map<String, List<String>>> mapCompletableFuture = CompletableFuture.supplyAsync(() -> getCommentByListPostId(keySet));

        // 是否点赞
        try {
            User loginUser = UserUtils.getLoginUser(request);
            List<CollectThumbVo> collectThumbList = baseMapper.selectCTByPostIds(keySet, loginUser.getId());
            for (CollectThumbVo collectThumbVo : collectThumbList) {
                String collectPostId = collectThumbVo.getCollectPostId();
                if (StringUtils.hasText(collectPostId)) {
                    List<PostVo> postVos = postIdMap.get(collectPostId);
                    if (!CollectionUtils.isEmpty(postVos)) {
                        PostVo postVo = postVos.get(0);
                        if (postVo != null) {
                            postVo.setHasCollect(true);
                        }
                    }
                }
                String thumbPostId = collectThumbVo.getThumbPostId();
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
        } catch (Exception e) {
            //无登录不做特殊处理
        }
        Map<String, List<String>> commentByListPostId;
        try {
            commentByListPostId = mapCompletableFuture.get();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
        for (String postVoId : keySet) {
            PostVo postVo = postIdMap.get(postVoId).get(0);
            postVo.setCommentList(commentByListPostId.get(postVoId));
        }
        return postIdMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
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

    private Map<String, List<String>> getCommentByListPostId(Collection<String> postIds) {
        Map<String, List<String>> hashMap = new HashMap<>(postIds.size());
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
        String replyId = commentRequest.getReplyId();
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

            // 判断是回复，还是评论
            if (StringUtils.hasText(replyId)) {
                postComment.setReplyId(replyId);
            }
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
        if (!loginUserId.equals(userId)&&!UserUtils.isAdmin(loginUser)) {
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
}
