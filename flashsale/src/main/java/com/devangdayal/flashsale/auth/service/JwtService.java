package com.devangdayal.flashsale.auth.service;

import com.devangdayal.flashsale.user.entity.User;

public interface JwtService {
    String generateToken(User user);
    String extractUsername(String token);
    boolean isTokenValid(String token, User user);
    long getExpirationTime();
}