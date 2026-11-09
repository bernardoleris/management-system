CREATE TABLE email_verification_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    administrator_id BIGINT NOT NULL,
    company_membership_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    consumed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_email_verification_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_email_verification_token_administrator
        FOREIGN KEY (administrator_id) REFERENCES administrator (id) ON DELETE CASCADE,
    CONSTRAINT fk_email_verification_token_membership
        FOREIGN KEY (company_membership_id) REFERENCES company_membership (id) ON DELETE CASCADE,
    INDEX idx_email_verification_token_administrator_created (administrator_id, created_at)
);
