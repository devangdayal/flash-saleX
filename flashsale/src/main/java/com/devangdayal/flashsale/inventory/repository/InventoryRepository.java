package com.devangdayal.flashsale.inventory.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.devangdayal.flashsale.inventory.entity.Inventory;
import java.util.List;


@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    List<Inventory> findByProductId(Long productId);
    List<Inventory> findByProductIdAndAvailableQuantity(Long productId, Integer availableQuantity);
    
}
