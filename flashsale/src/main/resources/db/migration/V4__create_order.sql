CREATE TABLE
    orders (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        product_id BIGINT NOT NULL,
        quantity INT NOT NULL,
        status VARCHAR(20) NOT NULL,
        created_at TIMESTAMP NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_order_product FOREIGN KEY (product_id) REFERENCES product (id)
    );