package com.user.py.mode.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author ice
 * @Date 2023/3/21 10:59
 * @Description: TODO
 */
@Data
public class ChatList{
    private String friendId;
    private String name;
    private String avatarUrl;
    private List<ChatVo> chat;
}
