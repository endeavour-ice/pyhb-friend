package com.user.py.service;

import com.user.py.mapper.MessageMqMapper;
import com.user.py.mapper.UserMapper;
import com.user.py.mode.domain.MessageMq;
import com.user.py.mode.domain.vo.UserAvatarVo;
import com.user.py.utils.ListUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;


@SpringBootTest
class UserServiceTest {
    @Resource
    private UserMapper userMapper;
    @Resource
    private MessageMqMapper messageMqMapper;
    @Test
    void getUserAvatarVoByIds() {
        List<String> collect = Arrays.asList("1", "2", "3");
        String ids = ListUtil.ListToString(collect);
        System.out.println(ids);
        List<UserAvatarVo> userAvatarVoByIds = userMapper.getUserAvatarVoByIds(ids);
        System.out.println(userAvatarVoByIds);

    }

    @Test
    void getMessageMq() {
        MessageMq messageMq = new MessageMq();
        messageMq.setMessageId("63f82f7f1eb0d7a9cd2b2cb5");
        messageMq.setMessageBody("12");
        System.out.println(messageMqMapper.insert(messageMq));
    }
    public static void main(String[] args) {
        String s = null;
        String s1 = s + "asd";
        System.out.println(s1);
    }
}