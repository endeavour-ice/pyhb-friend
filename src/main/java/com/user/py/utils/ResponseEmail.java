package com.user.py.utils;

import lombok.Data;

/**
 * @Author ice
 * @Date 2022/9/27 11:18
 * @PackageName:com.user.oss.util
 * @ClassName: ResponseEmail
 * @Description:
 * @Version 1.0
 */
@Data
public class ResponseEmail {
    // 用户名
    private String userAccount;
    // 邮箱
    private String email;
    // 修改的邮箱
    private String updateEmail;
}
