package com.user.py.mode.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/2/14 10:53
 * @Description: Post 返回的数据
 */
@Data
public class PostVo implements Serializable {
    private static final long serialVersionUID = -7434749056772195178L;
    private String id;
    private String content;
    private Integer thumb;
    private Integer collect;
    private PostUserVo postUserVo;
    private String tag;
    private List<CommentVo> commentList;
    private boolean hasThumb =false;
    private boolean hasCollect = false;
    private LocalDateTime createTime;

}

