package com.devangdayal.flashsale.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.devangdayal.flashsale.user.entity.User;



@Repository
public interface UserRepository extends JpaRepository<User , Long> {
    List<User> findByNameContainingIgnoreCase(String name);
    List<User> findByEmailContainingIgnoreCase(String email);
    List<User> findByRoleContainingIgnoreCase(String role);
    List<User> findByEnabledContainingIgnoreCase(String enabled);
    List<User> findByEmailVerified(boolean emailVerified);
}
