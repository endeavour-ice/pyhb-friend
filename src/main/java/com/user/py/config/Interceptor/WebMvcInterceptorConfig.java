package com.user.py.config.Interceptor;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @Author ice
 * @Date 2023/3/18 11:18
 * @Description: TODO
 */
@Configuration
public class WebMvcInterceptorConfig implements WebMvcConfigurer {

    /**
     * 拦截器配置
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InterceptorConfig()).addPathPatterns("/**");
    }

    /**
     *请求参数拦截处理配置
     */

    @Override
    public void addArgumentResolvers(@NotNull List<HandlerMethodArgumentResolver> resolvers) {
    }
    /**
     * 返回值拦截处理配置
     */
    @Override
    public void addReturnValueHandlers(@NotNull List<HandlerMethodReturnValueHandler> handlers) {

    }


}



