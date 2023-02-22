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
 * 评论表
 * </p>
 *
 * @author ice
 * @since 2023-02-15
 */
@TableName("post_comment")
@ApiModel(value = "PostComment对象", description = "评论表")
@Data
public class PostComment implements Serializable {
    private static final long serialVersionUID = -432782447183343833L;
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @ApiModelProperty("帖子id")
    private String postId;

    @ApiModelProperty("评论用户 id")
    private String userId;


    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("是否删除")
    @TableLogic
    private Integer isDelete;


}
