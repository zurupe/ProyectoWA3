package com.proyecto.api_gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuración adicional para Spring Cloud Gateway
 */
@Configuration
public class GatewayConfig {

    private static final Logger logger = LoggerFactory.getLogger(GatewayConfig.class);

    /**
     * Filtro personalizado para logging de requests
     */
    @Component
    public static class RequestLoggingGatewayFilterFactory 
            extends AbstractGatewayFilterFactory<RequestLoggingGatewayFilterFactory.Config> {
        
        private static final Logger log = LoggerFactory.getLogger(RequestLoggingGatewayFilterFactory.class);

        public RequestLoggingGatewayFilterFactory() {
            super(Config.class);
        }

        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                if (config.isPreLogger()) {
                    log.info("🔍 Pre GatewayFilter logging: {} {}", 
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI());
                }
                
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    if (config.isPostLogger()) {
                        ServerHttpResponse response = exchange.getResponse();
                        log.info("📤 Post GatewayFilter logging: {} {} -> {}", 
                                exchange.getRequest().getMethod(),
                                exchange.getRequest().getURI(),
                                response.getStatusCode());
                    }
                }));
            };
        }

        public static class Config {
            private boolean preLogger = true;
            private boolean postLogger = true;

            public boolean isPreLogger() {
                return preLogger;
            }

            public void setPreLogger(boolean preLogger) {
                this.preLogger = preLogger;
            }

            public boolean isPostLogger() {
                return postLogger;
            }

            public void setPostLogger(boolean postLogger) {
                this.postLogger = postLogger;
            }
        }
    }

    /**
     * Filtro para manejo de errores personalizados
     */
    @Component
    public static class CustomErrorGatewayFilterFactory 
            extends AbstractGatewayFilterFactory<CustomErrorGatewayFilterFactory.Config> {

        private static final Logger log = LoggerFactory.getLogger(CustomErrorGatewayFilterFactory.class);

        public CustomErrorGatewayFilterFactory() {
            super(Config.class);
        }

        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                return chain.filter(exchange)
                    .onErrorResume(throwable -> {
                        log.error("❌ Error en Gateway para {}: {}", 
                                exchange.getRequest().getURI(), 
                                throwable.getMessage());
                        
                        ServerHttpResponse response = exchange.getResponse();
                        response.setStatusCode(HttpStatus.BAD_GATEWAY);
                        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
                        
                        String errorBody = String.format(
                            "{\"error\":\"Gateway Error\",\"message\":\"%s\",\"path\":\"%s\"}", 
                            throwable.getMessage(), 
                            exchange.getRequest().getPath().toString()
                        );
                        
                        return response.writeWith(
                            Mono.just(response.bufferFactory().wrap(errorBody.getBytes()))
                        );
                    });
            };
        }

        public static class Config {
            // Configuración futura si es necesaria
        }
    }
}