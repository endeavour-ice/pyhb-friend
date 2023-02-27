package com.user.py.mode.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/2/14 10:53
 * @Description: Post 返回的数据
 */
@Data
public class PostVo implements Serializable {
    private static final long serialVersionUID = -7434749056772195178L;
    // TODO
    private String id;
    private String content;
    private Integer thumb;
    private UserAvatarVo userAvatarVo;
    private String tag;
    private List<String> commentList;
    private boolean hasThumb;

}

