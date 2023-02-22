package com.user.py.mode.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ice
 * @date 2022/6/14 16:46
 */
@Data
public class UserLoginRequest implements Serializable {
    private String userAccount;
    private String password;
}
