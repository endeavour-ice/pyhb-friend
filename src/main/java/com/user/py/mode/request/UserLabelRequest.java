package com.user.py.mode.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author ice
 * @date 2022/9/16 20:47
 */
@Data
public class UserLabelRequest implements Serializable {
    private static final long serialVersionUID = 8023661918044130305L;

    @ApiModelProperty("标签类型")
    private String labelType;

    @ApiModelProperty("标签")
    private String label;
}
