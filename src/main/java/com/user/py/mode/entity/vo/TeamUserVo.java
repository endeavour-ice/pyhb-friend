package com.user.py.mode.entity.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 返回队伍的用户
 *
 * @author ice
 * @since 2022-06-14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamUserVo extends TeamUserAvatarVo {


    private static final long serialVersionUID = 4751948160720884461L;



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
