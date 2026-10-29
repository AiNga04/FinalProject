CREATE TABLE refresh_token
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    revoked_at TIMESTAMP(6) NULL,

    CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_account FOREIGN KEY (account_id)
        REFERENCES account(id) ON DELETE CASCADE,
    INDEX ix_refresh_token_account (account_id),
    INDEX ix_refresh_token_expires_at (expires_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
