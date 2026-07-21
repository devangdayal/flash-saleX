package com.devangdayal.flashsale.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devangdayal.flashsale.order.entity.Order;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByProductId(Long productId);
    Optional<Order> findByUserIdAndProductId(Long userId, Long productId);
}