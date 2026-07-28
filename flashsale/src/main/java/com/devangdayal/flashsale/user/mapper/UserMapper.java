package com.devangdayal.flashsale.user.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import com.devangdayal.flashsale.user.entity.User;
import com.devangdayal.flashsale.user.enums.UserRole;
import com.devangdayal.flashsale.auth.dto.RegisterRequest;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request) {

        return User.builder()
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .password(request.getPassword())
        .role(UserRole.USER)
        .enabled(true)
        .emailVerified(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
    }

}