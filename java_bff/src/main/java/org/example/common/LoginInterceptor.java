package org.example.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * 登录态拦截器。
 *
 * <p>本版为<b>存在性校验</b>（token / userToken 非空且长度合理）。
 * 项目当前无 JWT 密钥与统一会话校验实现——如后续接入，需在此补充验签。
 * 风险已登记于 plan task-001（临时方案）。</p>
 */
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoginInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String token = request.getHeader("token");
        String userToken = request.getHeader("userToken");

        if (isBlank(token) && isBlank(userToken)) {
            log.debug("未登录访问: {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":\"401\",\"message\":\"未登录\",\"result\":null}");
            return false;
        }
        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
