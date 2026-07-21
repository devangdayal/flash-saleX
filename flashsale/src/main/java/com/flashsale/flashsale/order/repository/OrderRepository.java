package com.flashsale.flashsale.order.repository;

import com.flashsale.flashsale.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByInventoryId(Long inventoryId);
    Optional<Order> findByOrderNumber(String orderNumber);
}