package com.flashsale.flashsale.order.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order {

    private Long id;
    private Long userId;
    private Long inventoryId;
    private Integer quantity;
    private Double totalPrice;
}