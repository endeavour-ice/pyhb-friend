package com.user.py.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ice
 * @date 2022/7/13 12:14
 */
@Data
public class DemoUser {
    @ExcelProperty("姓名")
    private String planetCode;
    @ExcelProperty("身份证")
    private String username;
}
