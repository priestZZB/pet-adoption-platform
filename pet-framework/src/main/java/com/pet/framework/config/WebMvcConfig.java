package com.pet.framework.config;

import com.pet.framework.interceptor.AuthInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Value("${pet.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 把 /uploads/** 映射到配置的物理目录
        String location = "file:" + uploadPath + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
        log.info("静态资源映射已配置: /uploads/** → {}", location);
    }

    @Autowired
    private com.pet.framework.interceptor.RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    private com.pet.framework.interceptor.MaintenanceInterceptor maintenanceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 维护模式拦截器（最高优先级，维护时拦截所有 API）
        registry.addInterceptor(maintenanceInterceptor)
                .addPathPatterns("/**")
                .order(0);

        // 限流拦截器（优先执行）
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns(
                        "/api/user/login",
                        "/api/user/login/phone",
                        "/api/user/register",
                        "/api/user/password/reset",
                        "/api/sms/code",
                        "/api/ai/chat",
                        "/api/file/upload",
                        "/api/file/upload/multi"
                );

        // JWT 认证拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/uploads/**"
                );
    }
}
