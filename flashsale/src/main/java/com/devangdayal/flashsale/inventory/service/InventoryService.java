package com.devangdayal.flashsale.inventory.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.devangdayal.flashsale.inventory.entity.Inventory;
import com.devangdayal.flashsale.inventory.repository.InventoryRepository;
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
    
}
