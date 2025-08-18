package com.proyecto.authservice.service;

import com.proyecto.authservice.entity.RefreshToken;
import com.proyecto.authservice.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.refresh-token.expiration:604800}") // default 7 days
    private long refreshTokenDurationSec;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(String username) {
        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plusSeconds(refreshTokenDurationSec);
        RefreshToken refreshToken = new RefreshToken(token, username, expiry);
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteByUsername(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }
}
