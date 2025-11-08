package com.be.ebooki.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // POST 요청 막는거 방지
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/payment/**").permitAll() // ✅ 카카오페이 경로 허용
                        .anyRequest().permitAll() // (임시로 전부 허용)
                );
        return http.build();
    }
}
