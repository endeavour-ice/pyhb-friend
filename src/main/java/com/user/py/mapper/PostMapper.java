package com.user.py.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.user.py.mode.entity.Post;
import com.user.py.mode.entity.vo.CollectThumbVo;
import com.user.py.mode.entity.vo.CommentVo;
import com.user.py.mode.entity.vo.PostVo;
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

    List<PostVo> selectIndexByPage(@Param("pageNum") long pageNum, @Param("pageSize") long pageSize,@Param("sorted") int sorted);

    int selectCountById();

    List<CollectThumbVo> selectCTByPostIds(@Param("postIds")Collection<String> postIds,@Param("userId") String userId);
}