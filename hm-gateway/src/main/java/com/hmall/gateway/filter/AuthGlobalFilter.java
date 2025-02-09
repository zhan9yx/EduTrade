

package com.hmall.gateway.filter;

import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.util.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;


@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AuthProperties.class)
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    // 用于处理 antPath 路径匹配
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 获取 request
        ServerHttpRequest request = exchange.getRequest();
        // 2. 判断是否需要登录拦截
        if (isExclude(request.getPath().toString())) {
            // 放行
            return chain.filter(exchange);
        }
        // 3. 获取token (header是map形式存储 用get获取)
        String token = null;
        List<String> headers = request.getHeaders().get("authorization");
        if (headers != null && !headers.isEmpty()) {
            token = headers.get(0);
        }
        // 4. 校验并解析
        Long userId = null;
        try {
            userId = jwtTool.parseToken(token);
        } catch (UnauthorizedException e) {
            // 拦截逻辑 设置当前状态码为401
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // 到此终止，后续拦截器不会再走
            return response.setComplete();
        }
        // 5. 用户信息保存到请求头，传递用户信息
        // System.out.println("userId = " + userId);
        String userInfo = userId.toString();
        ServerWebExchange swe = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        // 6. 放行
        return chain.filter(swe);
    }

    private boolean isExclude(String string) {
        for (String excludePath : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(excludePath, string)) {
                // 排除路径匹配
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
