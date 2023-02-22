package com.user.py.mode.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 队伍聊天记录表
 * </p>
 *
 * @author ice
 * @since 2022-09-12
 */
@TableName("team_chat_record")
@ApiModel(value = "TeamChatRecord对象", description = "队伍聊天记录表")
@Data
public class TeamChatRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    @ApiModelProperty("用户id")
    private String userId;

    @ApiModelProperty("队伍id")
    private String teamId;

    @ApiModelProperty("消息")
    private String message;

    @ApiModelProperty("是否已读 0 未读")
    private Integer hasRead;

    private LocalDateTime createTime;

    @ApiModelProperty("是否删除")
    private Integer isDelete;


}
