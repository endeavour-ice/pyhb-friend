package com.user.py.mode.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2022/11/15 13:07
 * @Description: TODO
 */
@Data
public class UserIdRequest implements Serializable {
    private static final long serialVersionUID = 6843161168564275030L;
    private String id;

}
