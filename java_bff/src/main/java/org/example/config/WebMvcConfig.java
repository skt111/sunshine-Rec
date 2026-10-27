package org.example.config;

import org.example.common.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 配置：注册登录态拦截器。
 *
 * <p>本地联调可通过 {@code supply-radar.auth.enabled=false} 关闭校验；
 * 生产环境保持 true。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 是否启用登录态校验（本地联调可关） */
    @Value("${supply-radar.auth.enabled:true}")
    private boolean authEnabled;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (!authEnabled) {
            // 本地联调：跳过登录态校验（仅 /api/probe/open 仍可访问）
            return;
        }
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/probe/open", "/error");
    }
}
