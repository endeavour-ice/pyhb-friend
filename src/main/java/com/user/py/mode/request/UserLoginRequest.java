package com.user.py.mode.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ice
 * @date 2022/6/14 16:46
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = -414895823618106467L;
    private String uuid;
    private String code;
    private String userAccount;
    private String password;
}
