CREATE TABLE
    inventory (
        product_id BIGINT PRIMARY KEY,
        available_quantity INT NOT NULL,
        reserved_quantity INT NOT NULL DEFAULT 0,
        version BIGINT NOT NULL DEFAULT 0,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE
    );