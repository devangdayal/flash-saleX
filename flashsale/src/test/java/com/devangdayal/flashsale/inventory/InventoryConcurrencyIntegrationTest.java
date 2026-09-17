package com.devangdayal.flashsale.inventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.devangdayal.flashsale.common.exception.inventory.InsufficientInventoryException;
import com.devangdayal.flashsale.inventory.entity.Inventory;
import com.devangdayal.flashsale.inventory.repository.InventoryRepository;
import com.devangdayal.flashsale.inventory.service.InventoryService;
import com.devangdayal.flashsale.product.entity.Product;
import com.devangdayal.flashsale.product.enums.ProductStatus;
import com.devangdayal.flashsale.product.repository.ProductRepository;

@SpringBootTest
@Testcontainers
public class InventoryConcurrencyIntegrationTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private static final int INITIAL_STOCK = 100;
    private static final int ATTEMPTS = 1_000;

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("jwt.secret",
                () -> "test-secret-that-is-at-least-thirty-two-characters-long");
        registry.add("jwt.expiration", () -> "3600000");
        registry.add("jwt.refresh-expiration", () -> "604800000");
    }

    @Test
    void reserveInventory_underHighContention_neverOversells() throws Exception {
        Product product = createProductWithInventory(INITIAL_STOCK);

        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {

            CountDownLatch ready = new CountDownLatch(ATTEMPTS);
            CountDownLatch start = new CountDownLatch(1);

            List<Future<Boolean>> futures = new ArrayList<>();

            for (int i = 0; i < ATTEMPTS; i++) {
                futures.add(executorService.submit(() -> {
                    ready.countDown();
                    start.await();

                    try {
                        inventoryService.reserveInventory(product.getId(), 1);
                        return true;
                    } catch (InsufficientInventoryException ex) {
                        return false;
                    }
                }));
            }

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            int successfulReservations = 0;
            for (Future<Boolean> future : futures) {
                if (future.get(30, TimeUnit.SECONDS)) {
                    successfulReservations++;
                }
            }

            Inventory inventory = inventoryRepository
                    .findById(product.getId())
                    .orElseThrow();

            assertEquals(INITIAL_STOCK, successfulReservations);
            assertEquals(0, inventory.getAvailableQuantity());
            assertEquals(INITIAL_STOCK, inventory.getReservedQuantity());
        }
    }

    // Helper function for Insufficient Inventory Check
    private Product createProductWithInventory(int stock) {
        Product product = Product.builder()
                .name("Flash Sale Product")
                .description("Test product")
                .price(new BigDecimal("999.99"))
                .status(ProductStatus.ACTIVE)
                .build();

        Product savedProduct = productRepository.saveAndFlush(product);

        Inventory inventory = Inventory.builder()
                .product(savedProduct)
                .availableQuantity(stock)
                .reservedQuantity(0)
                .build();

        inventoryRepository.saveAndFlush(inventory);

        return savedProduct;
    }

    @Test
    void reserveInventory_withZeroQuantity_shouldFail() {
        Product product = createProductWithInventory(10);

        assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.reserveInventory(product.getId(), 0));
    }

    @Test
    void reserveInventory_withNegativeQuantity_shouldFail() {
        Product product = createProductWithInventory(10);

        assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.reserveInventory(product.getId(), -1));
    }

    @Test
    void reserveInventory_whenStockIsExhausted_shouldNotChangeInventory() {
        Product product = createProductWithInventory(1);

        inventoryService.reserveInventory(product.getId(), 1);

        assertThrows(
                InsufficientInventoryException.class,
                () -> inventoryService.reserveInventory(product.getId(), 1));

        Inventory inventory = inventoryRepository
                .findById(product.getId())
                .orElseThrow();

        assertEquals(0, inventory.getAvailableQuantity());
        assertEquals(1, inventory.getReservedQuantity());
    }

}
