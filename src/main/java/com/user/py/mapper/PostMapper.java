package com.user.py.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.user.py.mode.entity.Post;
import com.user.py.mode.entity.vo.*;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 帖子 Mapper 接口
 * </p>
 *
 * @author ice
 * @since 2023-02-13
 */
public interface PostMapper extends BaseMapper<Post> {
    List<CommentVo> getPostCommentByPostIds(@Param("postIds") Collection<String> postIds);

    List<PostVo> selectPostByUserOrderPage(@Param("pageNum") long pageNum, @Param("pageSize") long pageSize, @Param("sorted") int sorted,@Param("userId")String userId);

    List<PostUserVo> selectPostThumbTotal(@Param("userIdList") Collection<String> userIdList);
    int selectCountById(@Param("userId") String userId);

    List<CollectThumbVo> selectCTByPostIds(@Param("postIds")Collection<String> postIds,@Param("userId") String userId);


    PostVo selectPostUserOrderById(@Param("id") String postId);

    List<PostVo> selectPostCollectByUserId(@Param("userId") String userId);

    List<PostVo> searchContent(@Param("content") String content);
    List<PostVo> searchUser(@Param("userId") String userId);
}