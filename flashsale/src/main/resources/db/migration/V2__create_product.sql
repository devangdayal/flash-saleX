CREATE TABLE
    product (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT,
        price NUMERIC(12, 2) NOT NULL,
        status VARCHAR(30) NOT NULL,
        created_at TIMESTAMP NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMP NOT NULL DEFAULT NOW ()
    );

CREATE INDEX idx_product_name ON product (name);

CREATE INDEX idx_product_id ON product (id);