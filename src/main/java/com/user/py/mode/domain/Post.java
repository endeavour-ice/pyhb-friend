package com.user.py.mode.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * 帖子
 * </p>
 *
 * @author ice
 * @since 2023-02-13
 */
@ApiModel(value = "Post对象", description = "帖子")
@Data
public class Post implements Serializable {


    private static final long serialVersionUID = 8664786476499747747L;
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @ApiModelProperty("创建用户 id")
    private String userId;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("标签id")
    private String tagId;

    @ApiModelProperty("状态（0-待审核, 1-通过, 2-拒绝）")
    private Integer reviewStatus;

    @ApiModelProperty("审核信息")
    private String reviewMessage;

    @ApiModelProperty("浏览数")
    private Integer viewNum;

    @ApiModelProperty("点赞数")
    private Integer thumbNum;

    @ApiModelProperty("创建时间")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updateTime;

    @ApiModelProperty("是否删除")
    @TableLogic
    private Integer isDelete;


}
