package com.hmall.common.config;

import com.hmall.common.interceptors.UserInfoInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnClass(DispatcherServlet.class)
// 以DispatcherServlet类作为条件，微服务都有，但是网关没有（没有用SpringMVC），因此可以把微服务排除在外
// 不然会和网关的拦截器冲突（基于WebFlux）
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 默认拦截所有路径
        registry.addInterceptor(new UserInfoInterceptor());
    }
}
