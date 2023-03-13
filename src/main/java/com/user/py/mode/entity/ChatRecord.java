package com.user.py.mode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 聊天记录表
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
@TableName("chat_record")
@ApiModel(value = "ChatRecord对象", description = "聊天记录表")
@Data
public class ChatRecord implements Serializable {


    private static final long serialVersionUID = -4824601743400604129L;
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @ApiModelProperty("用户id ")
    private String userId;

    @ApiModelProperty("好友id")
    private String friendId;

    @ApiModelProperty("是否已读 0 未读")
    private Integer hasRead;
    // 发送的时间
    private String sendTime;

    private Date createTime;

    @ApiModelProperty("是否删除")
    @TableLogic
    private Integer isDelete;

    @ApiModelProperty("消息")
    private String message;

}
