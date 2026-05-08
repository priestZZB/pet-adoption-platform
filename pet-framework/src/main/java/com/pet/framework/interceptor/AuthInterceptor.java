package com.pet.framework.interceptor;

import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.UnauthorizedException;
import com.pet.framework.annotation.RequireRole;
import com.pet.framework.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证拦截器
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1. 没有 Token 的请求直接放行（有 @RequireRole 的方法在后面拦截）
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            // 检查方法/类上是否有 @RequireRole，有则要求登录
            HandlerMethod hm = (HandlerMethod) handler;
            RequireRole requireRole = hm.getMethodAnnotation(RequireRole.class);
            if (requireRole == null) {
                requireRole = hm.getBeanType().getAnnotation(RequireRole.class);
            }
            if (requireRole != null) {
                throw new UnauthorizedException(ResultCodeEnum.TOKEN_INVALID);
            }
            return true;
        }
        String token = authHeader.substring(BEARER_PREFIX.length());

        // 2. 解析 JWT
        Claims claims;
        try {
            claims = jwtUtils.parseToken(token);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ResultCodeEnum.TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new UnauthorizedException(ResultCodeEnum.TOKEN_INVALID);
        }

        // 3. 用户信息存入 request
        request.setAttribute("userId", claims.getSubject());
        request.setAttribute("role", claims.get("role", String.class));

        // 4. 校验 @RequireRole 权限（从数据库查询最新角色，而非依赖JWT里可能过期的缓存）
        HandlerMethod hm = (HandlerMethod) handler;
        RequireRole methodRole = hm.getMethodAnnotation(RequireRole.class);
        RequireRole classRole = hm.getBeanType().getAnnotation(RequireRole.class);
        RequireRole requiredRole = (methodRole != null) ? methodRole : classRole;
        if (requiredRole != null) {
            Long userId = Long.valueOf(claims.getSubject());
            List<String> dbRoles = jdbcTemplate.queryForList(
                    "SELECT r.role_code FROM sys_user_role ur JOIN sys_role r ON ur.role_id = r.id WHERE ur.user_id = ?",
                    String.class, userId);
            String[] requiredValues = requiredRole.value();
            boolean matched = dbRoles.stream().anyMatch(
                    r -> Arrays.stream(requiredValues).anyMatch(req -> req.equals(r)));
            if (!matched) {
                throw new UnauthorizedException(ResultCodeEnum.ROLE_REQUIRED);
            }
        }

        return true;
    }
}