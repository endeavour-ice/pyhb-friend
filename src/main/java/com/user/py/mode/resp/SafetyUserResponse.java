package com.user.py.mode.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2023/1/12 19:59
 * @Description: 去敏
 */
@Data
public class SafetyUserResponse implements Serializable {

    private static final long serialVersionUID = -3112768827503614001L;

    private String id;

    private String username;

    private String userAccount;

    private String avatarUrl;

    private String gender;
    /**
     * 标签
     */
    private String tags;
    /**
     * 个人描述
     */
    private String profile;

    private String tel;

    private String email;

    private String status;

}
