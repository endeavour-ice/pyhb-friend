package com.user.py.mode.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ice
 * @date 2022/6/14 16:35
 */

@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = -5152160556516051584L;
    // userAccount, password, checkPassword
    private String userAccount;
    private String password;
    private String checkPassword;
    private String planetCode;
    private String email;
    private String code;
}
