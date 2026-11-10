CREATE TABLE account_avatar (
    id BIGINT AUTO_INCREMENT NOT NULL,
    account_id BIGINT NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    uploaded_by BIGINT NULL,
    uploaded_at DATETIME NOT NULL,
    deleted_at DATETIME NULL,
    restored_at DATETIME NULL,
    CONSTRAINT pk_account_avatar PRIMARY KEY (id),
    CONSTRAINT fk_avatar_account FOREIGN KEY (account_id) REFERENCES account(id),
    CONSTRAINT fk_avatar_uploader FOREIGN KEY (uploaded_by) REFERENCES account(id)
);

CREATE INDEX ix_avatar_account ON account_avatar(account_id);
CREATE INDEX ix_avatar_current ON account_avatar(account_id, is_current);
CREATE INDEX ix_avatar_status ON account_avatar(status);
