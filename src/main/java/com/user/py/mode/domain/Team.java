package com.user.py.mode.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍表
 *
 * @TableName team
 */
@TableName(value = "team")
@Data
public class Team implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 2232493452646148672L;
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 队伍的名称
     */
    private String name;

    /**
     * 用户id
     */
    private String userId;

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
     * 队伍头像
     */
    private String avatarUrl;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;


}