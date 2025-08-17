package com.proyecto.authservice.config;

import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {
        /**
         * Codificador JWT
         */
        @Bean
        public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
                System.out.println("[DEBUG] Creando bean JwtEncoder en AuthorizationServerConfig");
                return new NimbusJwtEncoder(jwkSource);
        }

        /**
         * Configuración del servidor de autorización OAuth2
         */
        @Bean
        @Order(1)
        public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
                        throws Exception {
                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .anyRequest().authenticated())
                                .csrf(csrf -> csrf.ignoringRequestMatchers(
                                                new MediaTypeRequestMatcher(MediaType.APPLICATION_JSON)))
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
                // OIDC deshabilitado, solo OAuth2 puro
                http.setSharedObject(org.springframework.security.web.AuthenticationEntryPoint.class,
                                new LoginUrlAuthenticationEntryPoint("/login"));

                return http.build();
        }

        /**
         * Configuración de seguridad por defecto
         */
        @Bean
        @Order(2)
        public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
                        throws Exception {
                http
                                .authorizeHttpRequests((authorize) -> authorize
                                                // Endpoints públicos
                                                .requestMatchers("/api/auth/register", "/api/auth/public/**")
                                                .permitAll()
                                                .requestMatchers("/actuator/**", "/health", "/error").permitAll()
                                                .requestMatchers("/.well-known/**").permitAll()
                                                // Endpoints protegidos
                                                .requestMatchers("/api/auth/me", "/api/auth/users/**").authenticated()
                                                .anyRequest().authenticated())
                                // Desactivar CSRF para APIs REST
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/api/**", "/oauth2/**", "/.well-known/**"))
                                // Configurar para APIs REST y formulario web
                                .formLogin(formLogin -> formLogin
                                                .loginPage("/login")
                                                .permitAll())
                                // Configurar HTTP Basic para APIs
                                .httpBasic(Customizer.withDefaults());

                return http.build();
        }

        /**
         * Repositorio de clientes registrados
         */
        @Bean
        public RegisteredClientRepository registeredClientRepository() {
                // Cliente para el frontend Angular
                RegisteredClient frontendClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("frontend-client")
                                .clientSecret(passwordEncoder().encode("frontend-secret"))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .redirectUri("http://localhost:4200/login/oauth2/code/frontend-client")
                                .redirectUri("http://localhost:4200/authorized")
                                .postLogoutRedirectUri("http://localhost:4200/")
                                .scope("read")
                                .scope("write")
                                .clientSettings(ClientSettings.builder()
                                                .requireAuthorizationConsent(false)
                                                .build())
                                .tokenSettings(TokenSettings.builder()
                                                .accessTokenTimeToLive(Duration.ofHours(1))
                                                .refreshTokenTimeToLive(Duration.ofDays(1))
                                                .reuseRefreshTokens(false)
                                                .build())
                                .build();
                // El bean JwtEncoder debe estar fuera de este método

                // Cliente para microservicios (machine-to-machine)
                RegisteredClient microserviceClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("microservice-client")
                                .clientSecret(passwordEncoder().encode("microservice-secret"))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .scope("read")
                                .scope("write")
                                .scope("admin")
                                .tokenSettings(TokenSettings.builder()
                                                .accessTokenTimeToLive(Duration.ofMinutes(30))
                                                .build())
                                .build();

                // Cliente para pruebas con password grant (solo para desarrollo)
                RegisteredClient testClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("test-client")
                                .clientSecret(passwordEncoder().encode("test-secret"))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                .redirectUri("http://localhost:8080/login/oauth2/code/test-client")
                                .scope("read")
                                .scope("write")
                                .clientSettings(ClientSettings.builder()
                                                .requireAuthorizationConsent(true)
                                                .build())
                                .build();

                return new InMemoryRegisteredClientRepository(frontendClient, microserviceClient, testClient);
        }

        /**
         * Fuente de claves JWT
         */
        @Bean
        public JWKSource<SecurityContext> jwkSource() {
                KeyPair keyPair = generateRsaKey();
                RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
                RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
                RSAKey rsaKey = new RSAKey.Builder(publicKey)
                                .privateKey(privateKey)
                                .keyID(UUID.randomUUID().toString())
                                .build();
                JWKSet jwkSet = new JWKSet(rsaKey);
                return new ImmutableJWKSet<>(jwkSet);
        }

        /**
         * Generador de claves RSA
         */
        private static KeyPair generateRsaKey() {
                KeyPair keyPair;
                try {
                        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                        keyPairGenerator.initialize(2048);
                        keyPair = keyPairGenerator.generateKeyPair();
                } catch (Exception ex) {
                        throw new IllegalStateException(ex);
                }
                return keyPair;
        }

        /**
         * Decodificador JWT
         */
        @Bean
        public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
                return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
        }

        /**
         * Configuraciones del servidor de autorización
         */
        @Bean
        public AuthorizationServerSettings authorizationServerSettings() {
                return AuthorizationServerSettings.builder()
                                .issuer("http://localhost:8081")
                                .build();
        }

        /**
         * Codificador de contraseñas
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
