package com.user.py.mode.request;

import lombok.Data;

/**
 * @Author ice
 * @Date 2023/3/14 22:21
 * @Description: 接受和拒绝好友的请求
 */
@Data
public class RejectRequest {
    // 接受
    String acceptId;
    // 拒绝
    String refuseId;
}
