package com.user.py.mode.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/2/15 19:23
 * @Description: TODO
 */
@Data
public class PostCommentVo implements Serializable {
    private static final long serialVersionUID = 5165195235507901572L;
    private String id;
    private String userId;
    private String content;
    private UserAvatarVo userVo;
    private List<CommentReplyVo> commentReplyVoList;
}
