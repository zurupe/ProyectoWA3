package com.proyecto.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

/**
 * Configuración de seguridad para API Gateway con OAuth2 Resource Server
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    /**
     * Configuración de la cadena de filtros de seguridad
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Deshabilitar CSRF para APIs REST
                .csrf(csrf -> csrf.disable())
                
                // Configurar autorización de requests
                .authorizeExchange(exchanges -> exchanges
                        // Endpoints públicos del gateway
                        .pathMatchers("/actuator/**", "/actuator/gateway/**").permitAll()
                        
                        // Endpoints de OAuth2 (no requieren token, son para obtenerlo)
                        .pathMatchers("/oauth2/**", "/.well-known/**").permitAll()
                        
                        // Endpoints públicos de auth-service
                        .pathMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .pathMatchers("/api/auth/check-username/**", "/api/auth/check-email/**").permitAll()
                        
                        // Endpoints públicos de servicios (health checks, etc.)
                        .pathMatchers("/*/actuator/health", "/api/*/health").permitAll()
                        
                        // Todos los demás endpoints requieren autenticación
                        .anyExchange().authenticated()
                )
                
                // Configurar OAuth2 Resource Server con JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(reactiveJwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                
                // Configurar CORS (también configurado en application.yml)
                .cors(Customizer.withDefaults())
                
                .build();
    }

    /**
     * Configuración del decodificador JWT reactivo
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    /**
     * Convertidor de autenticación JWT para extraer authorities del claim 'role'
     */
    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            if (role != null) {
                return Collections.singletonList(new SimpleGrantedAuthority(role));
            }
            return Collections.emptyList();
        });
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    /**
     * Configuración CORS adicional (redundante con application.yml pero más específica)
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(List.of("http://localhost:*"));
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}