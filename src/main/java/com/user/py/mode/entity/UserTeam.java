package com.user.py.mode.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍表
 * @TableName user_team
 */
@TableName(value ="user_team")
@Data
public class UserTeam implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = -4639540624582420418L;
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
     * 队伍id
     */
    private String teamId;

    /**
     * 加入队伍时间
     */
    private Date joinTime;

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