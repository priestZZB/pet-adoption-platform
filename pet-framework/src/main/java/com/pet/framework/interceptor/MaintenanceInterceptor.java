package com.pet.framework.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 全站维护模式拦截器
 *
 * 当 pet.maintenance.enabled=true 时，所有 /api/** 请求返回 503，
 * 前端拦截器捕获 503 后自动跳转到 /maintenance 页面。
 *
 * 放行的路径：
 *   - /uploads/**  上传的文件（图片等）
 *   - /api/banners  轮播图（维护页用这个接口检测服务是否恢复）
 *   - 非 /api/ 路径  前端页面
 */
@Slf4j
@Component
public class MaintenanceInterceptor implements HandlerInterceptor {

    @Value("${pet.maintenance.enabled:false}")
    private boolean maintenanceEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (!maintenanceEnabled) {
            return true;
        }

        String path = request.getRequestURI();

        // 前端页面正常访问（非 API 路径），让用户看到维护页面
        if (!path.startsWith("/api/")) {
            return true;
        }

        // 放行：上传文件访问
        if (path.startsWith("/uploads/")) {
            return true;
        }

        // 放行：维护状态查询接口（前端维护页面用它检测是否恢复）
        if (path.equals("/api/maintenance/status")) {
            return true;
        }

        // 放行：轮播图接口（维护页面兜底检测）
        if (path.equals("/api/banners")) {
            return true;
        }

        // 放行：Swagger 文档
        if (path.startsWith("/swagger") || path.startsWith("/v3/api-docs") || path.startsWith("/webjars")) {
            return true;
        }

        log.info("维护模式拦截: {} → 503", path);
        sendMaintenanceResponse(response);
        return false;
    }

    private void sendMaintenanceResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
            "{\"code\":503,\"msg\":\"系统维护中，请稍后重试\"}"
        );
    }
}
