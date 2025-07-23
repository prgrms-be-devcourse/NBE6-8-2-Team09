package com.back.back9.domain.user.service;

import com.back.back9.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    @Value("${custom.jwt.secretKey}")
    private String secretKey;

    @Value("${custom.jwt.accessToken.expirationSeconds}")
    private int expirationSeconds;

    public String createToken(String userLoginId, String username) {
        Map<String, Object> body = Map.of(
                "userLoginId", userLoginId,
                "username", username,
                "type", "accessToken"
        );
        return Ut.jwt.toString(secretKey, expirationSeconds, body);
    }

    public boolean isTokenValid(String token) {
        return Ut.jwt.isValid(secretKey, token);
    }

    public String getUserLoginIdFromToken(String token) {
        Map<String, Object> payload = Ut.jwt.payload(secretKey, token);
        return payload == null ? null : (String) payload.get("userLoginId");
    }

    public String getUsernameFromToken(String token) {
        Map<String, Object> payload = Ut.jwt.payload(secretKey, token);
        return payload == null ? null : (String) payload.get("username");
    }
}
