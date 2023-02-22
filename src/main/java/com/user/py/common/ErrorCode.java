package com.user.py.common;

/**
 * 全局错误码
 */

public enum ErrorCode {
    ERROR(201, "失败",""),
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求数据为空",""),
    NO_LOGIN(40100,"未登录",""),
    NO_AUTH(40101, "权限不足", ""),
    SYSTEM_EXCEPTION(5000, "系统内部异常", "");
    // 错误状态码
    private final Integer code;
    // 错误信息
    private final String message;
    // 错误描述
    private final String description;

    ErrorCode(Integer code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
