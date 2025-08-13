package com.company.uavs.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Dev-only override to permit all requests when explicitly enabled.
 * Activate with: -Duavs.security.permitAll=true
 */
@Configuration
@ConditionalOnProperty(prefix = "uavs.security", name = "permitAll", havingValue = "true")
public class DevSecurityConfig {

    @Bean
    @Order(0)
    @ConditionalOnMissingBean(name = "filterChain")
    public SecurityFilterChain devPermitAllFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
        return http.build();
    }
}
