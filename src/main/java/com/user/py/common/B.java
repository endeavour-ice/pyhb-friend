package com.user.py.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ice
 * @date 2022/6/19 15:48
 */
@Data
public
class B<T> implements Serializable {
    private static final long serialVersionUID = -4586350006748247261L;
    private Integer code;
    private T data;
    private String message;
    private String description;
    private ErrorCode errorCode;

    public B(Integer code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public B(Integer code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public B(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public B(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public B(ErrorCode errorCode, String message, String description) {
        this.code = errorCode.getCode();
        this.message = message;
        this.description = description;
    }

    private B() {

    }

    public static <T> B<T> ok(T data) {
        B<T> b = new B<>();
        b.setCode(200);
        b.setMessage("ok");
        b.setData(data);
        return b;
    }

    public static <T> B<T> ok() {
        B<T> b = new B<>();
        b.setCode(200);
        b.setMessage("ok");
        b.setData(null);
        return b;
    }

    public static <T> B<T> error() {
        B<T> b = new B<>();
        b.setCode(201);
        b.setMessage("error");
        b.setData(null);
        return b;
    }
    public static <T> B<T> error(String description) {
        B<T> b = new B<>();
        b.setCode(201);
        b.setMessage("系统错误");
        b.setDescription(description);
        b.setData(null);
        return b;
    }
    public static <T> B<T> error(Integer code, String message, String description) {
        return new B<>(code, message, description);
    }

    public static <T> B<T> error(ErrorCode errorCode) {
        return new B<>(errorCode);
    }

    public static <T> B<T> error(ErrorCode errorCode, String message) {
        return new B<>(errorCode, message);
    }

    public static <T> B<T> error(ErrorCode errorCode, String message, String description) {
        return new B<>(errorCode, message, description);
    }

}
