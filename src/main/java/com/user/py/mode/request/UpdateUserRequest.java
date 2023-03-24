package com.user.py.mode.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2022/10/3 12:17
 * @PackageName:com.user.py.mode.request
 * @ClassName: UpdateUserRequest
 * @Description: 修改用户
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateUserRequest extends IdRequest implements Serializable {
    private static final long serialVersionUID = -4689454932816955493L;

    private String username;
    private String gender;
    private String tags;
    private String profile;
    private String email;
    private String tel;
    private String status;
    private String code;
}
