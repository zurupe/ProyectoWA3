package com.proyecto.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(unauthorizedEntryPoint()))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/health", "/api/auth/**").permitAll()
                        .anyExchange().authenticated())
                .cors(cors -> {
                });
        return http.build();
    }

    private ServerAuthenticationEntryPoint unauthorizedEntryPoint() {
        return (exchange, ex) -> {
            ServerWebExchange responseExchange = exchange;
            responseExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            // No WWW-Authenticate header set
            return responseExchange.getResponse().setComplete();
        };
    }
}
