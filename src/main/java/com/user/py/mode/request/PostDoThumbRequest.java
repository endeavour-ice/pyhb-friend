package com.user.py.mode.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 点赞 / 取消点赞请求
 *
 * @author yupi
 */
@Data
public class PostDoThumbRequest implements Serializable {

    /**
     * 帖子 id
     */
    private String postId;

    private static final long serialVersionUID = 1L;
}