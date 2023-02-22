package com.user.py.mode.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ice
 * @date 2022/9/16 22:43
 */
@Data
public class UserLabelResponse implements Serializable {
    private static final long serialVersionUID = -5798581433074712514L;
    @ApiModelProperty("标签类型")
    private String labelType;

    @ApiModelProperty("标签")
    private List<String> label;
}
