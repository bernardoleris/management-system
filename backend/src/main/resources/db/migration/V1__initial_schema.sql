CREATE TABLE address (
    id BIGINT NOT NULL AUTO_INCREMENT,
    street VARCHAR(100) NOT NULL,
    number VARCHAR(10) NOT NULL,
    complement VARCHAR(50),
    neighborhood VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    zip_code VARCHAR(8) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE permission (
    id BIGINT NOT NULL AUTO_INCREMENT,
    description VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_permission_description UNIQUE (description)
);

CREATE TABLE category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_category_name UNIQUE (name)
);

CREATE TABLE brand (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_brand_name UNIQUE (name)
);

CREATE TABLE supplier (
    id BIGINT NOT NULL AUTO_INCREMENT,
    trade_name VARCHAR(100) NOT NULL,
    cnpj VARCHAR(14) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_supplier_cnpj UNIQUE (cnpj)
);

CREATE TABLE tb_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    created_at DATETIME(6) NOT NULL,
    active BOOLEAN NOT NULL,
    account_non_expired BOOLEAN NOT NULL,
    account_non_locked BOOLEAN NOT NULL,
    credentials_non_expired BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_email UNIQUE (email)
);

CREATE TABLE administrator (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    login VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    last_login DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_administrator_user UNIQUE (user_id),
    CONSTRAINT uk_administrator_login UNIQUE (login),
    CONSTRAINT fk_administrator_user FOREIGN KEY (user_id) REFERENCES tb_user (id)
);

CREATE TABLE client (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    document_type VARCHAR(4) NOT NULL,
    document_number VARCHAR(14) NOT NULL,
    birth_date DATE,
    address_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_client_user UNIQUE (user_id),
    CONSTRAINT uk_client_document UNIQUE (document_number),
    CONSTRAINT uk_client_address UNIQUE (address_id),
    CONSTRAINT fk_client_user FOREIGN KEY (user_id) REFERENCES tb_user (id),
    CONSTRAINT fk_client_address FOREIGN KEY (address_id) REFERENCES address (id)
);

CREATE TABLE user_permission (
    user_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, permission_id),
    CONSTRAINT fk_user_permission_user FOREIGN KEY (user_id) REFERENCES tb_user (id),
    CONSTRAINT fk_user_permission_permission FOREIGN KEY (permission_id) REFERENCES permission (id)
);

CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    barcode VARCHAR(50),
    purchase_price DECIMAL(10, 2) NOT NULL,
    sale_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    active BOOLEAN NOT NULL,
    category_id BIGINT NOT NULL,
    brand_id BIGINT NOT NULL,
    supplier_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_product_barcode UNIQUE (barcode),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brand (id),
    CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (id)
);

CREATE TABLE sale (
    id BIGINT NOT NULL AUTO_INCREMENT,
    date DATETIME(6) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    discount DECIMAL(10, 2) NOT NULL,
    total_value DECIMAL(10, 2) NOT NULL,
    notes VARCHAR(200),
    admin_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_sale_administrator FOREIGN KEY (admin_id) REFERENCES administrator (id),
    CONSTRAINT fk_sale_client FOREIGN KEY (client_id) REFERENCES client (id)
);

CREATE TABLE sale_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_sale_item_sale FOREIGN KEY (sale_id) REFERENCES sale (id),
    CONSTRAINT fk_sale_item_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE TABLE stock_movements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    movement_type VARCHAR(255) NOT NULL,
    date DATETIME(6) NOT NULL,
    reason VARCHAR(255),
    observation VARCHAR(255),
    exit_reason VARCHAR(255),
    supplier_id BIGINT,
    sale_id BIGINT,
    admin_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_stock_movement_sale UNIQUE (sale_id),
    CONSTRAINT fk_stock_movement_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (id),
    CONSTRAINT fk_stock_movement_sale FOREIGN KEY (sale_id) REFERENCES sale (id),
    CONSTRAINT fk_stock_movement_administrator FOREIGN KEY (admin_id) REFERENCES administrator (id)
);

CREATE TABLE stock_movement_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    stock_movement_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    quantity_before INT,
    quantity_difference INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_stock_movement_item_movement FOREIGN KEY (stock_movement_id) REFERENCES stock_movements (id),
    CONSTRAINT fk_stock_movement_item_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE TABLE alert (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    admin_id BIGINT NOT NULL,
    alert_type VARCHAR(255) NOT NULL,
    minimum_quantity INT NOT NULL,
    message VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    read_at DATETIME(6),
    is_read BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_alert_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_alert_administrator FOREIGN KEY (admin_id) REFERENCES administrator (id)
);

CREATE TABLE refresh_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked BOOLEAN NOT NULL,
    administrator_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_administrator FOREIGN KEY (administrator_id) REFERENCES administrator (id)
);
