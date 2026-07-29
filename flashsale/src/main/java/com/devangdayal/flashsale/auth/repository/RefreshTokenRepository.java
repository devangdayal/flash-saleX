package com.devangdayal.flashsale.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.devangdayal.flashsale.auth.entity.RefreshToken;
import com.devangdayal.flashsale.user.entity.User;

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    

    @Modifying
    @Transactional
    @Query("delete from RefreshToken r where r.user = :user")
    void deleteByUser(@Param("user") User user);

    Optional<RefreshToken> findByUser(User user);

    boolean existsByToken(String token);
}
