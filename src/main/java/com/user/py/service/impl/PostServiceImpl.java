package com.user.py.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import com.user.py.mapper.PostMapper;
import com.user.py.mode.constant.PostSortedType;
import com.user.py.mode.domain.Post;
import com.user.py.mode.domain.PostThumb;
import com.user.py.mode.domain.User;
import com.user.py.mode.domain.UserLabel;
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
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    private IUserService userService;

    /**
     * 添加论坛
     *
     * @param postRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean addPost(AddPostRequest postRequest, User loginUser) {

        if (postRequest == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "请填写数据");
        }
        String content = postRequest.getContent();
        String tagId = postRequest.getTagId();
        String userId = postRequest.getUserId();
        String id = loginUser.getId();
        if (!StringUtils.hasText(content) || !StringUtils.hasText(tagId)) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
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
        return this.save(post);
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
        if (pageSize > 50) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
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
        Map<String, UserAvatarVo> userAvatarVoMap = userService.listByIds(userIds).stream().map(user -> {
            UserAvatarVo userAvatarVo = new UserAvatarVo();
            BeanUtils.copyProperties(user, userAvatarVo);
            return userAvatarVo;
        }).collect(Collectors.toMap(UserAvatarVo::getId, i -> i));
        Map<String, List<PostVo>> postIdListMap = postList.stream().map(post -> {
            PostVo postVo = new PostVo();
            postVo.setId(post.getId());
            postVo.setContent(post.getContent());
            postVo.setThumb(post.getThumbNum());
            if (labelMap != null && labelMap.size() > 0) {
                postVo.setTag(labelMap.get(post.getTagId()).getLabel());
            }
            postVo.setUserAvatarVo(userAvatarVoMap.get(post.getUserId()));
            postVo.setHasThumb(false);
            return postVo;
        }).collect(Collectors.groupingBy(PostVo::getId));

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

        postVOPage.setRecords(postIdListMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));
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


    @Override
    @Transactional(rollbackFor = Exception.class)
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
            boolean isDo = redisCache.isCachSet(postId, userId);
            if (isDo) {
                QueryWrapper<PostThumb> thumbQueryWrapper = new QueryWrapper<>();
                thumbQueryWrapper.eq("post_id", postId);
                thumbQueryWrapper.eq("user_id", userId);
                // 取消点赞
                result = thumbService.remove(thumbQueryWrapper);
                if (result) {
                    result = this.update()
                            .eq("id", postId)
                            .ge("thumb_num", 0)
                            .setSql("thumb_num=thumb_num-1").update();
                    redisCache.removeCachSet(postId, userId);
                    if (result && redisCache.removeCachSet(postId, userId)) {
                        return true;
                    } else {
                        throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
                    }
                } else {
                    throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
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
                    if (result && redisCache.addCacheSet(postId, userId, 30, TimeUnit.MINUTES) > 0) {
                        return true;
                    } else {
                        throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
                    }

                } else {
                    throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
                }
            }
        }
    }


}
