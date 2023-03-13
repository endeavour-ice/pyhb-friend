package com.user.py.mode.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ice
 * @date 2022/8/23 12:46
 */
@Data
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = -2119040467100327904L;
    /**
     * 队伍的名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Long maxNum;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 过期时间
     */
    private String expireTime;
}
