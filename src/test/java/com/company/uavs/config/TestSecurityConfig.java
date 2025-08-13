package com.company.uavs.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {
    
    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(
            HttpSecurity http,
            @Value("${test.security.permit-all:true}") boolean permitAll
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        if (permitAll) {
            http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
        } else {
            http
                .authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        }

        return http.build();
    }

    @Bean
    @Primary
    public JwtDecoder testJwtDecoder() {
        // Return a simple valid Jwt for any token during tests
        return token -> {
            Instant now = Instant.now();
            Map<String, Object> headers = Map.of("alg", "none");
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "test-user");
            claims.put("scope", "uavs.read uavs.write");
            return new org.springframework.security.oauth2.jwt.Jwt(
                token,
                now.minusSeconds(60),
                now.plusSeconds(3600),
                headers,
                claims
            );
        };
    }
}