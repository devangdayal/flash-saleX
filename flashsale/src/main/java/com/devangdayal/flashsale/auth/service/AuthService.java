package com.devangdayal.flashsale.auth.service;

import com.devangdayal.flashsale.auth.dto.AuthResponse;
import com.devangdayal.flashsale.auth.dto.LoginRequest;
import com.devangdayal.flashsale.auth.dto.RefreshTokenRequest;
import com.devangdayal.flashsale.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

}