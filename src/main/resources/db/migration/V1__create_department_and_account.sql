-- V1: Initial TestingSystem Database Schema

-- 1. DEPARTMENT
CREATE TABLE department
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(30) NOT NULL,

    -- BaseEntity
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    created_by BIGINT NULL,
    updated_at DATETIME(3) NULL,
    updated_by BIGINT NULL,
    timer DATETIME(3) NULL,

    -- Constraints
    CONSTRAINT pk_department PRIMARY KEY (id),
    CONSTRAINT uk_department_name UNIQUE (name),
    CONSTRAINT chk_department_status CHECK (status IN ('DELETED', 'INACTIVE', 'ACTIVE'))

) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;


-- 2. ACCOUNT
CREATE TABLE account
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,

    account_type VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE',
    department_id BIGINT NULL,

    -- BaseEntity
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    created_by BIGINT NULL,
    updated_at DATETIME(3) NULL,
    updated_by BIGINT NULL,
    timer DATETIME(3) NULL,

    -- Constraints
    CONSTRAINT pk_account PRIMARY KEY (id),
    CONSTRAINT uk_account_username UNIQUE (username),
    CONSTRAINT uk_account_email UNIQUE (email),
    CONSTRAINT fk_account_department FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL,
    CONSTRAINT chk_account_type CHECK (account_type IN ('ADMIN', 'MANAGER', 'EMPLOYEE')),
    CONSTRAINT chk_account_status CHECK (status IN ('DELETED', 'INACTIVE', 'ACTIVE')),
    INDEX ix_account_department (department_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
