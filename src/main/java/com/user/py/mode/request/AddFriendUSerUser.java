package com.user.py.mode.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2022/10/17 12:53
 * @PackageName:com.user.py.mode.request
 * @ClassName: AddFriendUSerUser
 * @Description: TODO
 * @Version 1.0
 */
@Data
public class AddFriendUSerUser implements Serializable {
    private static final long serialVersionUID = 5021026672617488725L;
    private String toUserId;
    private String message;
}
