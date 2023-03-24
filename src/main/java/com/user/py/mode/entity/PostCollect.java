package com.user.py.mode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 帖子收藏记录
 * </p>
 *
 * @author ice
 * @since 2023-03-10
 */
@TableName("post_collect")
@ApiModel(value = "PostCollect对象", description = "帖子收藏记录")
@Data
public class PostCollect implements Serializable {
    private static final long serialVersionUID = 8639595421388166573L;
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @ApiModelProperty("帖子 id")
    private String postId;

    @ApiModelProperty("创建用户 id")
    private String userId;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;


}
