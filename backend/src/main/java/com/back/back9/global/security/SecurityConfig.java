package com.back.back9.global.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationFilter customAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(
                                        "/", "/api/v1/users/login", "/api/v1/users/register",
                                        "/api/v1/users/register-admin", "/api/v1/users/logout",
                                        "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                                        "/oauth2/**", "/login/oauth2/**", "/login", "/error",
                                        "/favicon.ico", "/robots.txt", "/sitemap.xml",
                                        "/css/**", "/js/**", "/images/**", "/static/**"
                                ).permitAll()
                                .requestMatchers("/api/v1/adm/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"status\":\"fail\",\"code\":401,\"message\":\"인증이 필요합니다.\",\"result\":null}"
                            );
                        })
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authz ->
                                authz.baseUri("/oauth2/authorize")
                                        .authorizationRequestRepository(cookieAuthorizationRequestRepository)
                        )
                        .redirectionEndpoint(redir ->
                                redir.baseUri("/oauth2/callback/*")
                        )
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((req, res, ex) -> {
                            ex.printStackTrace();
                            String errorMsg;
                            if (ex instanceof OAuth2AuthenticationException oae) {
                                OAuth2Error err = oae.getError();
                                errorMsg = err.getErrorCode()
                                        + (err.getDescription() != null ? ": " + err.getDescription() : "");
                            } else {
                                errorMsg = ex.getMessage();
                            }
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write(
                                    String.format(
                                            "{\"status\":\"fail\",\"code\":401,\"message\":\"%s\"}",
                                            errorMsg.replace("\"", "\\\"")
                                    )
                            );
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/api/v1/users/logout")
                        .deleteCookies("accessToken", "apiKey", "role")
                        .logoutSuccessHandler((req, res, auth) -> {
                            res.setStatus(HttpServletResponse.SC_OK);
                        })
                        .permitAll()
                )
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
