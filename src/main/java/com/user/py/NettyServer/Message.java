package com.user.py.NettyServer;


import com.user.py.mode.entity.vo.ChatRecordVo;
import lombok.Data;

import java.io.Serializable;

/**
 * @author ice
 * @date 2022/7/25 16:14
 */
@Data
public class Message implements Serializable {
    private static final long serialVersionUID = -3975228717811142177L;
    private Integer type;// 消息的类型
    private ChatRecordVo chatRecord;//聊天的消息
    private Object ext; // 扩展
}
