package com.user.py.mode.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 返回队伍的用户
 *
 * @author ice
 * @since 2022-06-14
 */
@Data
public class TeamUserVo implements Serializable {


    private static final long serialVersionUID = 4751948160720884461L;
    private String id;

    /**
     * 队伍的名称
     */
    private String name;

    /**
     * 用户id
     */
    private String userId;

    private String avatarUrl;
    /**
     * 队伍id
     */
    private String teamId;
    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Long maxNum;

    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 状态
     */
    private Integer status;

    /**
     * 过期的时间
     */
    private Date expireTime;


    private List<UserVo> userVo = new ArrayList<>();
}
