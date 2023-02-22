package com.user.py.mode.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 回复评论表
 * </p>
 *
 * @author ice
 * @since 2023-02-16
 */
@TableName("comment_reply")
@ApiModel(value = "CommentReply对象", description = "回复评论表")
@Data
public class CommentReply implements Serializable {


    private static final long serialVersionUID = 7995718712165085988L;
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @ApiModelProperty("帖子id")
    private String postId;

    @ApiModelProperty("回复的id")
    private String commentId;

    @ApiModelProperty("创建用户 id")
    private String userId;

    @ApiModelProperty("评论内容")
    private String replyContent;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("是否删除")
    @TableLogic
    private Integer isDelete;


}
