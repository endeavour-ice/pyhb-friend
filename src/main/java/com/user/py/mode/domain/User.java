package com.user.py.mode.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author ice
 * @since 2022-06-14
 */
@Data
@ApiModel(value = "User对象", description = "用户表")
public class User implements Serializable{

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("登陆账号")
    private String userAccount;

    @ApiModelProperty("用户头像")
    private String avatarUrl;

    @ApiModelProperty("性别")
    private String gender;

    @ApiModelProperty("密码")
    private String password;
    /**
     * 标签
     */
    private String tags;

    /**
     * 个人描述
     */
    private String profile;

    @ApiModelProperty("手机号")
    private String tel;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("用户状态")
    private Integer userStatus;

    @ApiModelProperty("用户角色 ,判断是否是管理员")
    private Integer role;

    @ApiModelProperty("成员编号")
    private String planetCode;

    @ApiModelProperty("是否删除")
    @TableLogic
    private Integer isDelete;

    @ApiModelProperty("创建时间")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;

    @ApiModelProperty("修改时间")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updateTime;


}
