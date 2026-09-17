package com.devangdayal.flashsale.common.exception.inventory;

public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException() {
        super("Insufficient Inventory for this item.");
    }

    public InsufficientInventoryException(String message) {
        super(message);
    }
}