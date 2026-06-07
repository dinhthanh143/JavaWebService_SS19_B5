package com.example.demo.b5.service;

import com.example.demo.b5.entity.RefreshToken;
import com.example.demo.b5.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TokenRevocationService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final long refreshTokenExpirationMs = TimeUnit.DAYS.toMillis(7);

    @Transactional
    public String createRefreshToken(String username) {
        String tokenStr = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(refreshTokenExpirationMs);

        RefreshToken refreshToken = new RefreshToken(tokenStr, username, false, expiryDate);
        refreshTokenRepository.save(refreshToken);
        return tokenStr;
    }

    @Transactional(readOnly = true)
    public RefreshToken verifyAndGetRefreshToken(String tokenStr) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Mã làm mới không tồn tại trong hệ thống"));

        if (token.isRevoked()) {
            throw new RuntimeException("Mã làm mới này đã bị thu hồi từ trước bởi quản trị viên");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Mã làm mới đã hết hạn sử dụng, vui lòng đăng nhập lại");
        }

        return token;
    }

    @Transactional
    public void revokeAllSessionsOfUser(String username) {
        refreshTokenRepository.revokeAllByUsername(username);
    }
}