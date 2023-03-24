package com.user.py.annotation;

import com.user.py.mode.enums.UserRole;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthSecurity {
    UserRole[] isRole() default {};
    UserRole[] isNoRole() default {};

}
