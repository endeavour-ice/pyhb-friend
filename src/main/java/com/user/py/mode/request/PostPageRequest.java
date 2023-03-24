package com.user.py.mode.request;

import com.user.py.mode.dto.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2023/2/14 10:46
 * @Description: TODO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostPageRequest extends PageRequest implements Serializable  {
    private static final long serialVersionUID = 7013584199865595352L;
    @ApiModelProperty("是否是本人")
    private boolean isOwn=false;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("标签id")
    private String tagId;
    /**
     * 排序字段  1 - 升序， 2 - 降序， 3 - 点赞最多，4 - 评论最多
     */
    private Integer Sorted = 1;
}
