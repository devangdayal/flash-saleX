package com.devangdayal.flashsale.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devangdayal.flashsale.auth.entity.RefreshToken;
import com.devangdayal.flashsale.user.entity.User;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    Optional<RefreshToken> findByUser(User user);

    boolean existsByToken(String token);
}
