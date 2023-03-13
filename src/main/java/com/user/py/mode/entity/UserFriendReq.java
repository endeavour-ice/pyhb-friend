package com.user.py.mode.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
@TableName("user_friend_req")
@ApiModel(value = "UserFriendReq对象", description = "")
@Data
public class UserFriendReq implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    @ApiModelProperty("请求用户id")
    private String fromUserid;

    @ApiModelProperty("被请求好友用户")
    private String toUserid;

    @ApiModelProperty("发送的消息")
    private String message;

    @ApiModelProperty("消息是否已处理 0 未处理")
    private Integer userStatus;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;


}
