package com.back.back9.domain.user.service;

import com.back.back9.domain.user.entity.User;
import com.back.back9.standard.util.Ut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthTokenService {

    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    @Value("${custom.jwt.accessToken.expirationSeconds}")
    private int accessTokenExpirationSeconds;

    public String genAccessToken(User user) {
        Long id = user.getId();
        String userLoginId = user.getUserLoginId();
        String username = user.getUsername();
        String role = user.getRole().name();


        return Ut.jwt.toString(
                jwtSecretKey,
                accessTokenExpirationSeconds,
                Map.of(
                        "id", id,
                        "userLoginId", userLoginId,
                        "username", username,
                        "role", role
                )
        );
    }

    public Map<String, Object> payload(String accessToken) {
        Map<String, Object> parsedPayload = Ut.jwt.payload(jwtSecretKey, accessToken);

        if (parsedPayload == null) return null;


        Number idNum = (Number) parsedPayload.get("id");
        Long id = idNum != null ? idNum.longValue() : null;

        String userLoginId = (String) parsedPayload.get("userLoginId");
        String username = (String) parsedPayload.get("username");
        String role = (String) parsedPayload.get("role");

        return Map.of(
                "id", id,
                "userLoginId", userLoginId,
                "username", username,
                "role", role
        );
    }
}
