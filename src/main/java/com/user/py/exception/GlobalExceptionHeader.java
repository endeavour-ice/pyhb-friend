package com.user.py.exception;


import com.user.py.common.B;
import com.user.py.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.NoRouteToHostException;

/**
 * 全局异常处理器
 *
 * @author ice
 * @date 2022/6/19 18:21
 */
// 错误处理器,默认在这里
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHeader {
    // ExceptionHandlerExceptionResolver
    // TransientDataAccessResourceException
    @ExceptionHandler({GlobalException.class})
    public B<ErrorCode> businessExceptionHeader(GlobalException e) {
        log.error(e.getMessage(),e.getCode(),e.getDescription(),e);
        return B.error(e.getCode(),e.getMessage(),e.getDescription());
    }
    @ExceptionHandler({RuntimeException.class,Exception.class})
    public B<ErrorCode> runExceptionHeader(Exception e) {
        log.error("runException",e);
        return B.error(ErrorCode.SYSTEM_EXCEPTION.getCode(),ErrorCode.SYSTEM_EXCEPTION.getMessage(),"");
    }
    @ExceptionHandler({NoRouteToHostException.class})
    public B<ErrorCode> NoRouteToHostException(Exception e) {
        log.error("NoRouteToHostException",e);
        return B.error(ErrorCode.SYSTEM_EXCEPTION.getCode(),ErrorCode.SYSTEM_EXCEPTION.getMessage(),"连接异常请稍后。。。");
    }
}
