package com.hmall.api.config;

import com.hmall.api.client.fallback.ItemClientFallbackFactory;
import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

// 不需要configuration注解（配置类注解保证bean被扫描到）， 添加到Enable注解上就可以全局使用了
public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor() {
        // 匿名内部类
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                Long user = UserContext.getUser();
                if (user != null) {
                    requestTemplate.header("user-info", user.toString());
                }
            }
        };
    }

    @Bean
    public ItemClientFallbackFactory itemClientFallbackFactory() {
        return new ItemClientFallbackFactory();
    }

}
