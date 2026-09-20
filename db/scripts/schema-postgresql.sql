-- PostgreSQL Database Schema Creation Script

CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone_number VARCHAR(30),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    failed_login_attempts INT DEFAULT 0 NOT NULL,
    account_locked BOOLEAN DEFAULT FALSE NOT NULL,
    password_expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS app_roles (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS app_user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES app_roles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS payment_transactions (
    transaction_id VARCHAR(64) PRIMARY KEY,
    gateway_reference VARCHAR(128),
    provider VARCHAR(30) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(30) NOT NULL,
    handler_id VARCHAR(64),
    customer_id VARCHAR(64),
    message VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

INSERT INTO app_roles (role_name) VALUES ('ROLE_ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO app_roles (role_name) VALUES ('ROLE_USER') ON CONFLICT DO NOTHING;

INSERT INTO app_users (username, password_hash, email, first_name, last_name, status, password_expires_at)
VALUES ('admin', '$2a$10$e8q4R0Hh5mQ9V9/lX8M27eWwB1P6i1Kk.R9X5s4O6U5h4k3j2l1i.', 'admin@cthjob.com', 'System', 'Admin', 'ACTIVE', NOW() + INTERVAL '90 days')
ON CONFLICT (username) DO NOTHING;

INSERT INTO app_user_roles (user_id, role_id)
SELECT u.id, r.id FROM app_users u, app_roles r
WHERE u.username = 'admin' AND r.role_name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;
