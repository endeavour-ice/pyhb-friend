package com.user.py.mode.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2023/2/25 18:22
 * @Description: TODO
 */
@Data
public class CommentVo implements Serializable {
    private static final long serialVersionUID = 3289178661033578297L;
    private String postId;
    private String commentName;
    private String content;
    private String replyName;
}
