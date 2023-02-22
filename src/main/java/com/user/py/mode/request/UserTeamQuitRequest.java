package com.user.py.mode.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ice
 * @date 2022/9/18 17:19
 */
@Data
public class UserTeamQuitRequest implements Serializable {
    private static final long serialVersionUID = 899112876880502126L;
    private String teamId;
    private String userId;
}
