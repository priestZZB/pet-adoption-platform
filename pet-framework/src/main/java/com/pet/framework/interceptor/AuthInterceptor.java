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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1. 获取 Token（优先 Authorization Header，其次 query param token）
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            // 尝试从 query param 获取 token（支持 SSE 等无法设置 Header 的场景）
            String queryToken = request.getParameter("token");
            if (queryToken != null && !queryToken.isEmpty()) {
                authHeader = BEARER_PREFIX + queryToken;
            } else {
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

        // 检查 Token 是否已被加入黑名单（退出登录时加入）
        String jti = claims.getId();
        if (jti != null) {
            Boolean isBlacklisted = redisTemplate.hasKey(BLACKLIST_PREFIX + jti);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                throw new UnauthorizedException(ResultCodeEnum.TOKEN_INVALID);
            }
        }

        // 检查用户是否被管理员禁用
        Long uid = Long.valueOf(claims.getSubject());
        Integer userStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM sys_user WHERE id = ?", Integer.class, uid);
        if (userStatus != null && userStatus == 0) {
            // 把该用户的token也加入黑名单，强制退出
            if (jti != null) {
                redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "1", 24, TimeUnit.HOURS);
            }
            throw new UnauthorizedException(ResultCodeEnum.USER_DISABLED);
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
