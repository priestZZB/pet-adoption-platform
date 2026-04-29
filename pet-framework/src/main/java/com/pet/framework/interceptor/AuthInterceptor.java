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
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * JWT 认证拦截器
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1. 取 Token
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException(ResultCodeEnum.TOKEN_INVALID);
        }
        String token = authHeader.substring(BEARER_PREFIX.length());

        // 2. 解析 JWT（交给 JwtUtils）
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

        // 4. 校验 @RequireRole 权限
        HandlerMethod hm = (HandlerMethod) handler;
        RequireRole requireRole = hm.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = hm.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null) {
            String role = claims.get("role", String.class);
            boolean matched = Arrays.stream(requireRole.value())
                    .anyMatch(r -> r.equals(role));
            if (!matched) {
                log.warn("权限不足: userId={}, role={}, required={}",
                        claims.getSubject(), role, Arrays.toString(requireRole.value()));
                throw new UnauthorizedException(ResultCodeEnum.ROLE_REQUIRED);
            }
        }

        return true;
    }
}