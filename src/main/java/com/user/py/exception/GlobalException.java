package com.user.py.exception;


import com.user.py.common.ErrorCode;

/**
 * 全局异常
 *
 * @author ice
 * @date 2022/6/19 18:06
 */

public class GlobalException extends RuntimeException{
    private static final long serialVersionUID = -5358160169311975136L;
    private final Integer code;
    private final String description;

    public GlobalException(String message, Integer code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public GlobalException(ErrorCode errorCode,String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }
    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
