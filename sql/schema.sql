-- Cash Shop database schema
-- Target database: MySQL 8.0+
-- The application connects to the database named cash_shop.

CREATE DATABASE IF NOT EXISTS cash_shop
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cash_shop;

CREATE TABLE IF NOT EXISTS employees (
    matricule VARCHAR(64) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    salary DECIMAL(14, 2) NOT NULL DEFAULT 0,
    role VARCHAR(32) NOT NULL,
    PRIMARY KEY (matricule),
    UNIQUE KEY uq_employee_email (email),
    CONSTRAINT chk_employee_salary CHECK (salary >= 0),
    CONSTRAINT chk_employee_role CHECK (
        role IN ('ADMIN', 'MANAGER', 'COUNTER', 'AISLE_MANAGER', 'CASHIER', 'SECURITY', 'CLEANER')
    )
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS users (
    login VARCHAR(100) NOT NULL,
    password_user VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    PRIMARY KEY (login),
    UNIQUE KEY uq_user_email (email),
    CONSTRAINT fk_user_employee_email
        FOREIGN KEY (email) REFERENCES employees (email)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE OR REPLACE VIEW users_role AS
SELECT u.login, u.password_user, u.email, e.role
FROM users AS u
INNER JOIN employees AS e ON LOWER(TRIM(e.email)) = LOWER(TRIM(u.email));

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT NOT NULL,
    name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    total_spent DECIMAL(14, 2) NOT NULL DEFAULT 0,
    fidelity_points INT NOT NULL DEFAULT 0,
    PRIMARY KEY (customer_id),
    UNIQUE KEY uq_customer_phone (phone_number),
    CONSTRAINT chk_customer_total CHECK (total_spent >= 0),
    CONSTRAINT chk_customer_points CHECK (fidelity_points >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS products (
    reference INT NOT NULL,
    designation VARCHAR(200) NOT NULL,
    purchase_price DECIMAL(14, 2) NOT NULL,
    selling_price DECIMAL(14, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    PRIMARY KEY (reference),
    CONSTRAINT chk_product_purchase_price CHECK (purchase_price >= 0),
    CONSTRAINT chk_product_selling_price CHECK (selling_price >= 0),
    CONSTRAINT chk_product_stock CHECK (stock_quantity >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS electronic_products (
    reference INT NOT NULL,
    brand VARCHAR(100) NOT NULL,
    warranty INT NOT NULL DEFAULT 0,
    PRIMARY KEY (reference),
    CONSTRAINT fk_electronic_product
        FOREIGN KEY (reference) REFERENCES products (reference)
        ON DELETE CASCADE,
    CONSTRAINT chk_electronic_warranty CHECK (warranty >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS fresh_products (
    reference INT NOT NULL,
    expiration_date VARCHAR(32) NOT NULL,
    storage_temperature DECIMAL(6, 2) NOT NULL,
    PRIMARY KEY (reference),
    CONSTRAINT fk_fresh_product
        FOREIGN KEY (reference) REFERENCES products (reference)
        ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS artisanal_products (
    reference INT NOT NULL,
    type VARCHAR(100) NOT NULL,
    PRIMARY KEY (reference),
    CONSTRAINT fk_artisanal_product
        FOREIGN KEY (reference) REFERENCES products (reference)
        ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE OR REPLACE VIEW all_products AS
SELECT
    p.reference,
    p.designation,
    p.purchase_price,
    p.selling_price,
    p.stock_quantity,
    ep.brand AS electronic_brand,
    ep.warranty,
    fp.expiration_date,
    fp.storage_temperature,
    ap.type AS artisanal_type
FROM products AS p
LEFT JOIN electronic_products AS ep ON ep.reference = p.reference
LEFT JOIN fresh_products AS fp ON fp.reference = p.reference
LEFT JOIN artisanal_products AS ap ON ap.reference = p.reference;

CREATE TABLE IF NOT EXISTS aisles (
    aisle_code INT NOT NULL,
    aisle_name VARCHAR(150) NOT NULL,
    category VARCHAR(150) NOT NULL,
    aisle_chief VARCHAR(64) NULL,
    PRIMARY KEY (aisle_code),
    CONSTRAINT fk_aisle_chief
        FOREIGN KEY (aisle_chief) REFERENCES employees (matricule)
        ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS aisle_lines (
    aisle_code INT NOT NULL,
    reference INT NOT NULL,
    PRIMARY KEY (aisle_code, reference),
    CONSTRAINT fk_aisle_line_aisle
        FOREIGN KEY (aisle_code) REFERENCES aisles (aisle_code)
        ON DELETE CASCADE,
    CONSTRAINT fk_aisle_line_product
        FOREIGN KEY (reference) REFERENCES products (reference)
        ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS suppliers (
    code INT NOT NULL,
    supplier_name VARCHAR(150) NOT NULL,
    telephone VARCHAR(50) NOT NULL,
    address VARCHAR(100) NOT NULL,
    PRIMARY KEY (code)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS supplier_orders (
    order_number INT NOT NULL AUTO_INCREMENT,
    supplier_code INT NULL,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(14, 2) NOT NULL DEFAULT 0,
    PRIMARY KEY (order_number),
    CONSTRAINT fk_supplier_order_supplier
        FOREIGN KEY (supplier_code) REFERENCES suppliers (code)
        ON DELETE SET NULL,
    CONSTRAINT chk_supplier_order_status CHECK (
        status IN ('PENDING', 'APPROUVED', 'DENIDED', 'DELIVERED', 'COMPLETED')
    ),
    CONSTRAINT chk_supplier_order_total CHECK (total_amount >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS supplier_order_lines (
    order_number INT NOT NULL,
    reference INT NOT NULL,
    quantity INT NOT NULL,
    unit_purchase_price DECIMAL(14, 2) NOT NULL,
    PRIMARY KEY (order_number, reference),
    CONSTRAINT fk_supplier_line_order
        FOREIGN KEY (order_number) REFERENCES supplier_orders (order_number)
        ON DELETE CASCADE,
    CONSTRAINT fk_supplier_line_product
        FOREIGN KEY (reference) REFERENCES products (reference),
    CONSTRAINT chk_supplier_line_quantity CHECK (quantity > 0),
    CONSTRAINT chk_supplier_line_price CHECK (unit_purchase_price >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS sales (
    sale_id INT NOT NULL AUTO_INCREMENT,
    sale_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cashier VARCHAR(64) NULL,
    PRIMARY KEY (sale_id),
    CONSTRAINT fk_sale_cashier
        FOREIGN KEY (cashier) REFERENCES employees (matricule)
        ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS sale_lines (
    sale_id INT NOT NULL,
    product INT NOT NULL,
    line_number INT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (line_number),
    KEY idx_sale_lines_sale (sale_id),
    KEY idx_sale_lines_product (product),
    CONSTRAINT fk_sale_line_sale
        FOREIGN KEY (sale_id) REFERENCES sales (sale_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_sale_line_product
        FOREIGN KEY (product) REFERENCES products (reference)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS payments (
    payment_number INT NOT NULL,
    amount DECIMAL(14, 2) NOT NULL,
    payment_mode VARCHAR(32) NOT NULL,
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sale INT NOT NULL,
    PRIMARY KEY (payment_number),
    KEY idx_payments_sale (sale),
    CONSTRAINT fk_payment_sale
        FOREIGN KEY (sale) REFERENCES sales (sale_id)
        ON DELETE CASCADE,
    CONSTRAINT chk_payment_amount CHECK (amount >= 0),
    CONSTRAINT chk_payment_mode CHECK (
        payment_mode IN ('CASH', 'CREDIT_CARD', 'CARD', 'MOBILE_PAYMENT', 'MOBILE_MONEY')
    )
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS bills (
    bill_number INT NOT NULL,
    bill_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    customer INT NULL,
    cashier VARCHAR(64) NULL,
    sale INT NULL,
    PRIMARY KEY (bill_number),
    UNIQUE KEY uq_bill_sale (sale),
    CONSTRAINT fk_bill_customer
        FOREIGN KEY (customer) REFERENCES customers (customer_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bill_cashier
        FOREIGN KEY (cashier) REFERENCES employees (matricule)
        ON DELETE SET NULL,
    CONSTRAINT fk_bill_sale
        FOREIGN KEY (sale) REFERENCES sales (sale_id)
        ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS cash_register_sessions (
    session_id VARCHAR(36) NOT NULL,
    cashier VARCHAR(64) NOT NULL,
    last_heartbeat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (session_id),
    KEY idx_cash_register_heartbeat (last_heartbeat),
    CONSTRAINT fk_register_cashier
        FOREIGN KEY (cashier) REFERENCES employees (matricule)
        ON DELETE CASCADE
) ENGINE = InnoDB;
