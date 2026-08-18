package com.cetc28s.minichatjava.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * jwt解释工具类
 */
@Component
public class JwtTokenUtil {
    // 密钥
    @Value("${spring.jwt.secret_key}")
    private String secretKey;
    // 过期时间（毫秒），默认一天
    @Value("${spring.jwt.expiration_time}")
    private long expirationTime;

    // 生成Token
    public String generateToken(String value) {
        return Jwts.builder()
                .setSubject(value)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    // 从Token中提取所有声明（claims）
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    // 验证Token是否有效
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
