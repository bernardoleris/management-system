ALTER TABLE administrator
    ADD COLUMN token_version INT NOT NULL DEFAULT 0;

CREATE TABLE password_reset_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    administrator_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    consumed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_token_administrator
        FOREIGN KEY (administrator_id) REFERENCES administrator (id) ON DELETE CASCADE,
    INDEX idx_password_reset_token_administrator_created (administrator_id, created_at)
);
