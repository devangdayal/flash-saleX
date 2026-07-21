package com.devangdayal.flashsale.order.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.devangdayal.flashsale.order.entity.Order;
import com.devangdayal.flashsale.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }
    public List<Order> getOrders(Long userId){
        return orderRepository.findByUserId(userId);
    }

    
}
