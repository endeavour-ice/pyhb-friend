package com.user.py.mode.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author ice
 * @Date 2023/3/15 9:37
 * @Description: 根据idName
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IdNameRequest extends IdRequest{
    private String name;
    private static final long serialVersionUID = 1351806363289252156L;
}
