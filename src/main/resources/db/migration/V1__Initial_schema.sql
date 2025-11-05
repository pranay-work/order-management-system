-- Create orders table with indexes
CREATE TABLE IF NOT EXISTS orders (
    id BINARY(16) PRIMARY KEY,
    customer_id BINARY(16) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT DEFAULT 0 NOT NULL
) ENGINE=InnoDB;

-- Create order_items table with indexes
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BINARY(16) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Add indexes for performance
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Add version column for optimistic locking
ALTER TABLE orders ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;
