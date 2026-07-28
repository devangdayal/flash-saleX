package com.devangdayal.flashsale.auth.service;

import com.devangdayal.flashsale.auth.entity.RefreshToken;
import com.devangdayal.flashsale.user.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);
}