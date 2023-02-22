package com.user.py.controller.PartnerController;


import com.user.py.common.B;
import com.user.py.mode.domain.ChatRecord;
import com.user.py.service.IChatRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 聊天记录表 前端控制器
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
@RestController
//@CrossOrigin(origins = {"http://localhost:7777"}, allowCredentials = "true")
@RequestMapping("/partner/record")
public class ChatRecordController {

    @Autowired
    private IChatRecordService recordService;

    // 查询所有的聊天记录
    @GetMapping("/getList")
    public B<List<ChatRecord>> getAllRecordList(@RequestParam(required = false) String userId, String friendId, HttpServletRequest request) {
        List<ChatRecord> list =  recordService.selectAllList(userId, friendId, request);
        return B.ok(list);
    }
}
