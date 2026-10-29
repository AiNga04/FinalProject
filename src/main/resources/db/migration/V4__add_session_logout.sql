ALTER TABLE refresh_token ADD COLUMN session_id VARCHAR(36) NULL;
CREATE INDEX ix_refresh_token_session ON refresh_token(session_id);

CREATE TABLE revoked_session
(
    session_id VARCHAR(36) NOT NULL,
    account_id BIGINT NOT NULL,
    revoked_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_revoked_session PRIMARY KEY (session_id),
    CONSTRAINT fk_revoked_session_account FOREIGN KEY (account_id)
        REFERENCES account(id) ON DELETE CASCADE,
    INDEX ix_revoked_session_account (account_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
