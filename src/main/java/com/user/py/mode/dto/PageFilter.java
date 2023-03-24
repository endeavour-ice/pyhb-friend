package com.user.py.mode.dto;

import lombok.Data;

/**
 * @Author ice
 * @Date 2023/3/14 21:21
 * @Description: TODO
 */
@Data
public class PageFilter {
    private long current = 0;
    private long size = 30;

    public PageFilter(Long current, Long size) {
        if (current == null || size == null) {
            return;
        }
        if (--current >= 0 && size <= 30) {
            this.current = current * size;
            this.size = size;
        }
    }

}
