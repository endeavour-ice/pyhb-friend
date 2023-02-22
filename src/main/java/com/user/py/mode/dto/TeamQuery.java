package com.user.py.mode.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ice
 * @date 2022/8/22 16:41
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {
    private static final long serialVersionUID = -8132910430764423806L;

    /**
     * id
     */
    private Long id;

    /**
     * 搜索关键字 队伍和描述
     */
    private String searchTxt;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 队伍的名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;
    /**
     * 最大人数
     */
    private Integer maxNum;


    /**
     * 状态
     */
    private Integer status;


}
