package com.user.py.aop;

import com.user.py.annotation.AuthSecurity;
import com.user.py.common.B;
import com.user.py.mode.enums.UserRole;
import com.user.py.mode.enums.UserStatus;
import com.user.py.mode.entity.User;
import com.user.py.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author ice
 * @Date 2023/3/18 11:44
 * @Description: TODO
 */
@Aspect
@Component
@Slf4j
public class AuthSecurityAspect {

    //  权限控制
    @Around(value = "@annotation(authSecurity)")// 注解标注的
    public Object doAuth(ProceedingJoinPoint pjp, AuthSecurity authSecurity) throws Throwable {
        // 获得request对象
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        if (sra == null) {
            return B.error("请求过快，请稍后重试");
        }
        HttpServletRequest request = sra.getRequest();
        User loginUser = UserUtils.getLoginUser(request);
        Integer userRole = loginUser.getRole();
        UserRole[] role = authSecurity.isRole();

        UserRole[] noRole = authSecurity.isNoRole();
        if (role.length <= 0) {
            if (!userRole.equals(UserRole.NORMAL.getKey()) && !userRole.equals(UserRole.ADMIN.getKey())) {
                return B.error("权限不足");
            }
        } else {
            boolean is = false;
            for (UserRole r : role) {
                if (r.getKey() == userRole) {
                    is = true;
                    break;
                }
            }
            if (!is) {
                return B.error("权限不足");
            }
        }
        if (noRole.length > 0) {
            boolean is = false;
            for (UserRole r : noRole) {
                if (r.getKey() == userRole) {
                    is = true;
                    break;
                }
            }
            if (is) {
                return B.error("权限不足");
            }
        }
        // result的值就是被拦截方法的返回值
        return pjp.proceed();
    }

    @Around("execution(* com.user.py.controller..*.*(..))")
    public Object doUserStatusInterceptor(ProceedingJoinPoint point) throws Throwable {
        // 获取请求路径
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        try {
            User user = UserUtils.getLoginUser(httpServletRequest);
            if (user.getUserStatus().equals(UserStatus.LOCKING.getKey())) {
                return B.error("以封号，请联系管理员");
            }
        } catch (Exception ignored) {

        }
        return point.proceed();
    }
}
