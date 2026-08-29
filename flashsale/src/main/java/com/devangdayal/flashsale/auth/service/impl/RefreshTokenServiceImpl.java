package com.devangdayal.flashsale.auth.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devangdayal.flashsale.auth.entity.RefreshToken;
import com.devangdayal.flashsale.auth.repository.RefreshTokenRepository;
import com.devangdayal.flashsale.auth.service.RefreshTokenService;
import com.devangdayal.flashsale.common.exception.RefreshTokenExpiredException;
import com.devangdayal.flashsale.common.exception.RefreshTokenNotFoundException;
import com.devangdayal.flashsale.common.exception.RefreshTokenRevokedException;
import com.devangdayal.flashsale.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {

        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now()
                        .plusSeconds(refreshExpiration
                                / 1000))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);

    }

    @Override
    @Transactional
    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (refreshToken.isRevoked()) {
            throw new RefreshTokenRevokedException();
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenExpiredException();
        }
        return refreshToken;
    }
}
