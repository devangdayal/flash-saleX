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

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long userId, User updatedUser) {
        User existingUser = userRepository.findById(userId).orElse(null);
        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setRole(updatedUser.getRole());
            existingUser.setEnabled(updatedUser.getEnabled());
            existingUser.setEmailVerified(updatedUser.isEmailVerified());
            return userRepository.save(existingUser);
        }
        return null;
    }
}
