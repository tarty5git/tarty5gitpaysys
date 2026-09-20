-- Microsoft SQL Server Database Schema Creation Script

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[app_users]') AND type in (N'U'))
BEGIN
    CREATE TABLE app_users (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) NOT NULL UNIQUE,
        password_hash NVARCHAR(100) NOT NULL,
        email NVARCHAR(100),
        phone_number NVARCHAR(30),
        first_name NVARCHAR(50),
        last_name NVARCHAR(50),
        status NVARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
        failed_login_attempts INT DEFAULT 0 NOT NULL,
        account_locked BIT DEFAULT 0 NOT NULL,
        password_expires_at DATETIME2,
        created_at DATETIME2 DEFAULT GETDATE() NOT NULL,
        updated_at DATETIME2 DEFAULT GETDATE() NOT NULL
    );
END

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[app_roles]') AND type in (N'U'))
BEGIN
    CREATE TABLE app_roles (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        role_name NVARCHAR(50) NOT NULL UNIQUE
    );
END

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[app_user_roles]') AND type in (N'U'))
BEGIN
    CREATE TABLE app_user_roles (
        user_id BIGINT NOT NULL,
        role_id BIGINT NOT NULL,
        PRIMARY KEY (user_id, role_id),
        CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
        CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES app_roles(id) ON DELETE CASCADE
    );
END

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[payment_transactions]') AND type in (N'U'))
BEGIN
    CREATE TABLE payment_transactions (
        transaction_id NVARCHAR(64) PRIMARY KEY,
        gateway_reference NVARCHAR(128),
        provider NVARCHAR(30) NOT NULL,
        amount DECIMAL(19, 4) NOT NULL,
        currency NVARCHAR(10) NOT NULL,
        status NVARCHAR(30) NOT NULL,
        handler_id NVARCHAR(64),
        customer_id NVARCHAR(64),
        message NVARCHAR(255),
        created_at DATETIME2 DEFAULT GETDATE() NOT NULL
    );
END

IF NOT EXISTS (SELECT * FROM app_roles WHERE role_name = 'ROLE_ADMIN')
    INSERT INTO app_roles (role_name) VALUES ('ROLE_ADMIN');

IF NOT EXISTS (SELECT * FROM app_roles WHERE role_name = 'ROLE_USER')
    INSERT INTO app_roles (role_name) VALUES ('ROLE_USER');

IF NOT EXISTS (SELECT * FROM app_users WHERE username = 'admin')
BEGIN
    INSERT INTO app_users (username, password_hash, email, first_name, last_name, status, password_expires_at)
    VALUES ('admin', '$2a$10$e8q4R0Hh5mQ9V9/lX8M27eWwB1P6i1Kk.R9X5s4O6U5h4k3j2l1i.', 'admin@cthjob.com', 'System', 'Admin', 'ACTIVE', DATEADD(day, 90, GETDATE()));

    INSERT INTO app_user_roles (user_id, role_id)
    VALUES ((SELECT id FROM app_users WHERE username = 'admin'), (SELECT id FROM app_roles WHERE role_name = 'ROLE_ADMIN'));
END
