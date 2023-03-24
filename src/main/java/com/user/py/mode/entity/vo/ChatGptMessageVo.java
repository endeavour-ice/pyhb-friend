package com.user.py.mode.entity.vo;

import lombok.Data;

/**
 * @Author ice
 * @Date 2023/3/14 20:01
 * @Description: chat消息表
 */
@Data
public class ChatGptMessageVo {
    private Integer code;
    private String message;
    private Long timestamp;
    private String data;
}
