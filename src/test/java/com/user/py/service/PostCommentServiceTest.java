package com.user.py.service;

import com.user.py.mapper.PostMapper;
import com.user.py.mode.entity.vo.CommentVo;
import com.user.py.mode.entity.vo.PostVo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;

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
    @Test
    public void PostListTest() {
        List<PostVo> postVoList = postMapper.selectIndexByPage(0, 10,3);
        postVoList.forEach(postVo -> System.out.println(postVo.getThumb()));
    }

    public static void main(String[] args) {
        List<Integer> integers = Arrays.asList(4, 2,56,5);
        integers.stream().sorted((a,b)-> Integer.compare(b,a)).forEach(System.out::println);

    }
}
