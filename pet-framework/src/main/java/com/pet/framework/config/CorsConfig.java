package com.pet.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS 跨域配置
 * 允许的来源通过 application.yml 中的 pet.cors.allowed-origins 配置
 * 多个来源用逗号分隔
 */
@Configuration
public class CorsConfig {

    @Value("${pet.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        String[] origins = allowedOrigins.split(",");
        for (String origin : origins) {
            origin = origin.trim();
            if ("*".equals(origin)) {
                config.addAllowedOriginPattern("*");
            } else {
                config.addAllowedOrigin(origin);
            }
        }

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
