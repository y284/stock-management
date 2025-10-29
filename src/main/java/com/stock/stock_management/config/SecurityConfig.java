package com.stock.stock_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                // Disable CSRF for testing
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()                // Allow all endpoints
            )
            .headers(headers -> headers.frameOptions().disable()) // Allow H2 console if needed
            .formLogin(login -> login.disable())         // Disable form login
            .httpBasic(basic -> basic.disable());        // Disable basic auth

        return http.build();
    }
}
