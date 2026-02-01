package com.hugo.realapi.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtUtil jwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expMinutes}") long expMinutes
    ) {
        return new JwtUtil(secret, expMinutes);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtil jwt) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health", "/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthFilter(jwt), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
