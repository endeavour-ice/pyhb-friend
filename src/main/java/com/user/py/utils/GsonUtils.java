package com.user.py.utils;

import com.google.gson.Gson;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;

/**
 * @Author ice
 * @Date 2022/10/16 17:50
 * @PackageName:com.user.py.utils
 * @ClassName: GsonUtils
 * @Description: TODO 单例模式
 * @Version 1.0
 */
public class GsonUtils {
    // 防止反射破解
    private GsonUtils() {
        if (gson != null) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
    }

    private volatile static Gson gson;

    public static Gson getGson() {
        if (gson != null) {
            return gson;
        }
        synchronized (GsonUtils.class) {
            if (gson != null) {
                return gson;
            }
            gson = new Gson();
            return gson;
        }
    }
}
