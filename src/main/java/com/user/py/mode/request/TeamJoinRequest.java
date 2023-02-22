package com.user.py.mode.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户加入
 *
 * @author ice
 * @date 2022/8/29 16:24
 */
@Data
public class TeamJoinRequest implements Serializable {
    private static final long serialVersionUID = 7153870814095263196L;

    private String teamId;

    private String password;
}
