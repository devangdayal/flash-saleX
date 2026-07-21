package com.devangdayal.flashsale.user.service;

import org.springframework.stereotype.Service;
import com.devangdayal.flashsale.user.entity.User;
import com.devangdayal.flashsale.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}
