package com.back.back9.standard.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;

public class Ut {
    public static class jwt {
        public static String toString(String secret, int expireSeconds, Map<String, Object> body) {
            Date issuedAt = new Date();
            Date expiration = new Date(issuedAt.getTime() + 1000L * expireSeconds);
            Key secretKey = Keys.hmacShaKeyFor(secret.getBytes());

            return Jwts.builder()
                    .setClaims(body)
                    .setIssuedAt(issuedAt)
                    .setExpiration(expiration)
                    .signWith(secretKey)
                    .compact();
        }

        public static boolean isValid(String secret, String jwtStr) {
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
            try {
                Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(jwtStr);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public static Map<String, Object> payload(String secret, String jwtStr) {
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(jwtStr)
                        .getBody();
                return claims;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static class json {
        public static ObjectMapper objectMapper = new ObjectMapper();

        public static String toString(Object object) {
            return toString(object, null);
        }

        public static String toString(Object object, String defaultValue) {
            try {
                return objectMapper.writeValueAsString(object);
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }
}