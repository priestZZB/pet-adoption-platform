package com.pet.framework.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 工具类 — Token 生成与解析
 */
@Component
public class JwtUtils {

    @Value("${pet.jwt.secret:PetAdoptionPlatformSecretKey2024DefaultKeyThatIsLongEnoughForHS384!!}")
    private String secret;

    /**
     * Token 过期时间，从配置文件读取（默认24小时）
     * application.yml 中配置：pet.jwt.expiration=86400000
     */
    @Value("${pet.jwt.expiration:86400000}")
    private long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token（含 jti 唯一ID，用于退出时加入黑名单）
     * @param userId 用户ID
     * @param role   用户角色
     * @return JWT token 字符串
     */
    public String generateToken(String userId, String role) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 Token
     * @param token JWT token
     * @return Claims（包含 userId = subject, role = "role", jti = 唯一ID）
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取 Token 剩余有效时间（毫秒）
     */
    public long getRemainingTtl(Claims claims) {
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }
}