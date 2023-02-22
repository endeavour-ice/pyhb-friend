package com.user.py.mode.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ice
 * @date 2022/8/22 16:59
 */
@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 6536853198795443344L;
    /**
     * 页面大小
     */
    protected int pageSize = 10;
    /**
     * 当前第几页
     */
    protected int pageNum = 1;
}
