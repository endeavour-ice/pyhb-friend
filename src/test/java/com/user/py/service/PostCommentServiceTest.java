package com.user.py.service;

import com.user.py.mapper.PostMapper;
import com.user.py.mode.domain.vo.CommentVo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author ice
 * @Date 2023/2/25 19:20
 * @Description: TODO
 */
@SpringBootTest
public class PostCommentServiceTest {
    @Resource
    private PostMapper postMapper;
    @Test
    void PostCommentTest() {
        Set<String> ids = new HashSet<>();
        ids.add("1");
        ids.add("2");
        List<CommentVo> postCommentByPostIds =
                postMapper.getPostCommentByPostIds(ids);
        for (CommentVo postCommentByPostId : postCommentByPostIds) {
            System.out.println(postCommentByPostId);
        }

    }
}
