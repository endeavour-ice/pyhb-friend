package com.user.py.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @Author ice
 * @Date 2023/2/23 9:37
 * @Description: TODO
 */
public class ListUtil {

    public static String ListToString(List list) {
        return StringUtils.join(list.toArray(), ",");
    }
}
