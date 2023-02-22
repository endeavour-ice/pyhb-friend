package com.user.py.mode.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ice
 * @date 2022/8/23 12:46
 */
@Data
public class TeamUpdateRequest implements Serializable {

    private static final long serialVersionUID = 7722073575347437133L;
    /**
     * 队伍的id
     */
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
    private Date expireTime;
}
