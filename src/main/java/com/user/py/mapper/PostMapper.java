package com.user.py.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.user.py.mode.domain.Post;
import com.user.py.mode.domain.vo.CommentVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 帖子 Mapper 接口
 * </p>
 *
 * @author ice
 * @since 2023-02-13
 */
public interface PostMapper extends BaseMapper<Post> {
    List<CommentVo> getPostCommentByPostIds(@Param("postIds") Set<String> postIds);

}