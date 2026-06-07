package com.example.demo.b5.controller;

import com.example.demo.b5.entity.RefreshToken;
import com.example.demo.b5.service.JwtService;
import com.example.demo.b5.service.TokenRevocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenRevocationService tokenRevocationService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String username) {
        String accessToken = jwtService.generateAccessToken(username);
        String refreshToken = tokenRevocationService.createRefreshToken(username);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestParam String refreshToken) {
        try {
            RefreshToken validDbToken = tokenRevocationService.verifyAndGetRefreshToken(refreshToken);
            String newAccessToken = jwtService.generateAccessToken(validDbToken.getUsername());

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("refreshToken", refreshToken);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
    }

    @PostMapping("/revoke-all")
    public ResponseEntity<String> revokeAll(@RequestParam String username) {
        tokenRevocationService.revokeAllSessionsOfUser(username);
        return ResponseEntity.ok("Đã kích hoạt Panic Button: Thu hồi thành công toàn bộ các phiên hoạt động của " + username);
    }
}