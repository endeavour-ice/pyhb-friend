package com.user.py.mode.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2023/2/13 21:37
 * @Description: TODO
 */
@Data
public class AddPostRequest implements Serializable {
    private static final long serialVersionUID = 1578557702138636466L;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("标签id")
    private String tagId;


}
