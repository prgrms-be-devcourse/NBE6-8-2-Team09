package com.back.back9.global;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // H2 콘솔 사용 위해 비활성화
                .headers(headers -> headers.disable()) // H2 콘솔 frame 사용 위해
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**", "/coins/").permitAll() // h2-console 허용
                        .anyRequest().permitAll() // 그 외도 일단 다 허용 (개발용)
                );
        return http.build();
    }
}
