CREATE TABLE IF NOT EXISTS product_additional_files (
    product_id BIGINT NOT NULL,
    object_key VARCHAR(255) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
); 