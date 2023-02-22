package com.user.py.mode.socket_;

import lombok.Data;

/**
 * @author ice
 * @date 2022/7/13 20:32
 * 浏览器发送给服务器的数据
 */
@Data
public class Message {
    // 发送的人
    private String toName;
    // 发送的消息
    private String message;
}
