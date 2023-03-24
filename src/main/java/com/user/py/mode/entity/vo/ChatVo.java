package com.user.py.mode.entity.vo;

import com.user.py.mode.request.IdRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author ice
 * @Date 2023/3/21 10:26
 * @Description: 返回聊天Vo
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class ChatVo  extends IdRequest {
    private static final long serialVersionUID = 6113484189374723453L;
    private String friendId;
    private String userId;
    private String message;
    private String sendTime;
}
