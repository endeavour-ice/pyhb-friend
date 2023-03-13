package com.user.py.service.impl;

import com.user.py.mode.entity.ChatRecord;
import com.user.py.service.IChatRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class ChatRecordServiceImplTest {
    @Resource
    //private ChatRecordMapper chatRecordMapper;
    private IChatRecordService chatRecordService;


    @Test
    void selectAllList() {
        List<ChatRecord> records = chatRecordService.selectAllList("1574011394461388801", "1");
        System.out.println(records);
        //List<ChatRecord> chatRecords = chatRecordService.selectAllList("1574011394461388801", "1", null);
        //System.out.println(chatRecords);
    }
}