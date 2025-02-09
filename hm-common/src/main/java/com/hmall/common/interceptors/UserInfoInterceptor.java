package com.hmall.common.interceptors;

import cn.hutool.core.util.StrUtil;
import com.hmall.common.utils.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *   只做用户信息获取不做拦截
 */
public class UserInfoInterceptor implements HandlerInterceptor {
    /**
     * 在controller之前执行
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取用户登录信息 与网关保持一致
        String userInfo = request.getHeader("user-info");
        // 判断是否获取了用户 有则存入threadlocal
        if (StrUtil.isNotBlank(userInfo)) {
            UserContext.setUser(Long.valueOf(userInfo));
        }
        // 放行
        return true;
    }

    /**
     * 用户清理
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.removeUser();
    }
}
