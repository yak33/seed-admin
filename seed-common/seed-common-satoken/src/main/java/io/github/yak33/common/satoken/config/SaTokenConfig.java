package io.github.yak33.common.satoken.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 拦截配置：只拦截 /api 下接口（静态资源与页面路由不进鉴权链），登录接口放行
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> StpUtil.checkLogin()))
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/auth/login");
    }
}
