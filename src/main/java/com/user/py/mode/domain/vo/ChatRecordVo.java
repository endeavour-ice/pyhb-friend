package com.user.py.mode.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送的群聊消息
 * @author ice
 * @date 2022/9/11 21:35
 */
@Data
public class ChatRecordVo implements Serializable {
    private static final long serialVersionUID = -2461411320748791733L;
    // 用户id
    private String userId;
    //发送的id
    private String sendId;

    //"发送的消息"
    private String message;
}
