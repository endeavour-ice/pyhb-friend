package com.user.py.mode.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2023/3/6 15:34
 * @Description: TODO
 */
@Data
public class TeamUserAvatarVo implements Serializable {
    private static final long serialVersionUID = 6090482519840513272L;
    private String id;

    /**
     * 队伍的名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;
    /**
     * 用户id
     */
    private String userId;
    private boolean isCaptain=false;

    private String avatarUrl;
    /**
     * 队伍id
     */
    private String teamId;
}
