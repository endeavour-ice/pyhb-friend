package com.user.py.mode.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author ice
 * @Date 2023/3/23 20:41
 * @Description: 删除评论
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DelCommRequest extends IdRequest{
    private static final long serialVersionUID = -3982964672745220981L;
    private String postId;
}
