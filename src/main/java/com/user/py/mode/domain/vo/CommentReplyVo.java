package com.user.py.mode.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2023/2/16 10:34
 * @Description: TODO
 */
@Data
public class CommentReplyVo implements Serializable {
    private static final long serialVersionUID = 8973929544940035366L;

    private String id;

    private String postId;

    private String commentId;

    private String userId;

    private String replyContent;
}
