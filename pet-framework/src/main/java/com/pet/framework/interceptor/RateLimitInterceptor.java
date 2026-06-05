package com.pet.framework.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求限流拦截器
 *
 * 使用 Bucket4j 令牌桶算法，按 key（IP 或 userId）限流
 * 只对敏感接口生效：登录、注册、短信、AI、文件上传
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 限流桶缓存：key -> Bucket */
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    /** 各接口限流配置（每分钟次数） */
    @Value("${pet.rate-limit.login:5}")
    private int loginLimit;

    @Value("${pet.rate-limit.sms:1}")
    private int smsLimit;

    @Value("${pet.rate-limit.register:3}")
    private int registerLimit;

    @Value("${pet.rate-limit.ai:10}")
    private int aiLimit;

    @Value("${pet.rate-limit.upload:10}")
    private int uploadLimit;

    @Value("${pet.rate-limit.default:60}")
    private int defaultLimit;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws IOException {
        String uri = request.getRequestURI();
        String key = resolveKey(request, uri);

        int limit = getLimitForPath(uri);
        Bucket bucket = bucketCache.computeIfAbsent(key, k -> createBucket(limit));

        if (bucket.tryConsume(1)) {
            return true;
        }

        // 限流命中，返回 429
        log.warn("限流触发: key={}, uri={}, limit={}/分钟", key, uri, limit);
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Map.of("code", 429, "msg", "请求太频繁，请稍后再试", "data", null)
        ));
        return false;
    }

    /**
     * 解析限流 key：
     * - 已登录用户 → "user:用户ID"
     * - 未登录 → "ip:请求IP"
     */
    private String resolveKey(HttpServletRequest request, String uri) {
        String userId = (String) request.getAttribute("userId");
        if (userId != null) {
            return "user:" + userId;
        }
        // 从 header 取真实客户端 IP
        String ip = request.getHeader("CF-Connecting-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                int commaIdx = ip.indexOf(',');
                ip = (commaIdx > 0) ? ip.substring(0, commaIdx).trim() : ip.trim();
            } else {
                ip = request.getHeader("X-Real-IP");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
            }
        }
        return "ip:" + ip;
    }

    /**
     * 根据路径获取对应的限流阈值
     */
    private int getLimitForPath(String uri) {
        if (uri.contains("/login")) return loginLimit;
        if (uri.contains("/sms/code")) return smsLimit;
        if (uri.contains("/register")) return registerLimit;
        if (uri.contains("/ai/chat")) return aiLimit;
        if (uri.contains("/file/upload")) return uploadLimit;
        return defaultLimit;
    }

    /**
     * 创建令牌桶：1 分钟内最多消耗 limit 个令牌
     */
    private Bucket createBucket(int limit) {
        Bandwidth bandwidth = Bandwidth.classic(limit, Refill.greedy(limit, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
