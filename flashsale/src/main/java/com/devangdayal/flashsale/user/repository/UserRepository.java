package com.devangdayal.flashsale.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import com.devangdayal.flashsale.user.entity.User;
import com.devangdayal.flashsale.user.enums.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User , Long> {
    List<User> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
            String firstName,
            String lastName);
    List<User> findByEmailContainingIgnoreCase(String email);
    List<User> findByRoleContainingIgnoreCase(UserRole role);
    List<User> findByEnabledContainingIgnoreCase(String enabled);
    List<User> findByEmailVerified(boolean emailVerified);
    List<User> findByEnabled(boolean enabled);
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
