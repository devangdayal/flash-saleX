package com.devangdayal.flashsale.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devangdayal.flashsale.inventory.entity.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

  List<Inventory> findByProductId(Long productId);

  List<Inventory> findByProductIdAndAvailableQuantity(Long productId, Integer availableQuantity);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      UPDATE Inventory i
      SET i.availableQuantity = i.availableQuantity - :quantity,
          i.reservedQuantity = i.reservedQuantity + :quantity,
          i.version = i.version + 1
      WHERE i.productId = :productId
        AND i.availableQuantity >= :quantity
      """)
  int reserveIfAvailable(
      @Param("productId") Long productId,
      @Param("quantity") Integer quantity);
}
