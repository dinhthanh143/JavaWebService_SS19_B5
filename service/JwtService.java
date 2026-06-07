package com.example.demo.b5.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    private final String secretKey = System.getenv("JWT_SECRET_KEY") != null ?
            System.getenv("JWT_SECRET_KEY") : "RikkeiSoftSecretKeySuperSecureWithMoreThan32Characters2026";

    private final long accessTokenExpirationMs = TimeUnit.MINUTES.toMillis(15);

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateAccessToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new JwtException("Mã truy cập đã hết hạn sử dụng");
        } catch (MalformedJwtException | SignatureException e) {
            throw new JwtException("Chữ ký hoặc định dạng mã xác thực không hợp lệ");
        } catch (IllegalArgumentException e) {
            throw new JwtException("Yêu cầu chứa mã xác thực trống");
        }
    }
}