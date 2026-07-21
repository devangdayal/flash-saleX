CREATE TABLE
    orders (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        product_id BIGINT NOT NULL,
        order_number VARCHAR(64) NOT NULL UNIQUE,
        quantity INT NOT NULL,
        status VARCHAR(20) NOT NULL,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users (id),
        CONSTRAINT fk_order_product FOREIGN KEY (product_id) REFERENCES product (id)
    );

CREATE INDEX idx_order_number ON orders (order_number);

CREATE INDEX idx_order_user_id ON orders (user_id);

CREATE INDEX idx_order_product_id ON orders (product_id);

CREATE INDEX idx_order_status ON orders (status);