-- MyFinance Database Schema
-- Run this in MySQL Workbench or terminal to create the database structure

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        ENUM('USER','ADMIN') DEFAULT 'USER',
    status      ENUM('ACTIVE','INACTIVE','PENDING') DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Expenses table
CREATE TABLE IF NOT EXISTS expenses (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT NOT NULL,
    title       VARCHAR(150) NOT NULL,
    amount      DECIMAL(10,2) NOT NULL,
    category    VARCHAR(50) NOT NULL,
    expense_date DATE NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Budgets table
CREATE TABLE IF NOT EXISTS budgets (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    user_id       INT NOT NULL,
    category      VARCHAR(50) NOT NULL,
    limit_amount  DECIMAL(10,2) NOT NULL,
    spent_amount  DECIMAL(10,2) DEFAULT 0.00,
    start_date    DATE NOT NULL,
    end_date      DATE NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Goals table
CREATE TABLE IF NOT EXISTS goals (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    user_id       INT NOT NULL,
    title         VARCHAR(150) NOT NULL,
    description   TEXT,
    target_amount DECIMAL(10,2) NOT NULL,
    saved_amount  DECIMAL(10,2) DEFAULT 0.00,
    deadline      DATE NOT NULL,
    status        ENUM('IN_PROGRESS','ACHIEVED','FAILED') DEFAULT 'IN_PROGRESS',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- System settings table
CREATE TABLE IF NOT EXISTS system_settings (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    setting_key   VARCHAR(100) UNIQUE NOT NULL,
    setting_value VARCHAR(255) NOT NULL,
    updated_by    INT,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Audit logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    admin_id    INT NOT NULL,
    action      VARCHAR(255) NOT NULL,
    target_user INT,
    timestamp   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id)
);

-- Default admin account (password: admin123)
-- Note: This is a BCrypt hash for "admin123"
INSERT INTO users (name, email, password, role, status)
VALUES ('Admin', 'admin@myfinance.com',
'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'ACTIVE')
ON DUPLICATE KEY UPDATE email=email;

-- Default system settings
INSERT INTO system_settings (setting_key, setting_value) VALUES
('platform_name', 'MyFinance'),
('default_currency', 'INR'),
('session_timeout', '30'),
('max_login_attempts', '5'),
('budget_alerts_enabled', 'true'),
('email_reports_enabled', 'true')
ON DUPLICATE KEY UPDATE setting_key=setting_key;
