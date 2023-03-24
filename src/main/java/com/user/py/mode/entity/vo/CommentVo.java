package com.user.py.mode.entity.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author ice
 * @Date 2023/2/25 18:22
 * @Description: TODO
 */
@Data
public class CommentVo implements Serializable {
    private static final long serialVersionUID = 3289178661033578297L;
    private String postId;
    private String commentId;
    private String content;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;
    private PostUserVo owner;
    private boolean is_com = false;
}
