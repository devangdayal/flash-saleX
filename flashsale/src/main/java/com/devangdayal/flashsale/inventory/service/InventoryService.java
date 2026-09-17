package com.devangdayal.flashsale.inventory.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.devangdayal.flashsale.common.exception.inventory.InsufficientInventoryException;
import com.devangdayal.flashsale.inventory.entity.Inventory;
import com.devangdayal.flashsale.inventory.repository.InventoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;


    public List<Inventory> getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    public List<Inventory> getInventoryByProductIdAndAvailableQuantity(Long productId, Integer availableQuantity) {
        return inventoryRepository.findByProductIdAndAvailableQuantity(productId, availableQuantity);
    }

    @Transactional 
    public void reserveInventory(Long productId, Integer quantity){
        if(quantity == null || quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        int updatedRows = inventoryRepository.reserveIfAvailable(productId, quantity);

        if(updatedRows == 0){
            throw new InsufficientInventoryException();
        }
    }
    
}
