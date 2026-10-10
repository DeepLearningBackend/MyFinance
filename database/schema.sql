-- =======================================================
-- MyFinance Database Schema (9 Tables)
-- MySQL 8.0 & H2 MySQL Compatibility Mode
-- =======================================================

CREATE DATABASE IF NOT EXISTS myfinancedb 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE myfinancedb;

-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS system_settings;
DROP TABLE IF EXISTS spending_personality;
DROP TABLE IF EXISTS sms_queue;
DROP TABLE IF EXISTS goals;
DROP TABLE IF EXISTS budgets;
DROP TABLE IF EXISTS cash_entries;
DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS users;

-- 1. Users Table
CREATE TABLE users (
    id             INT PRIMARY KEY AUTO_INCREMENT,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(100) UNIQUE NOT NULL,
    password       VARCHAR(255) NOT NULL,
    role           ENUM('USER','ADMIN') DEFAULT 'USER',
    status         ENUM('ACTIVE','INACTIVE','PENDING') DEFAULT 'ACTIVE',
    monthly_income DECIMAL(10,2) DEFAULT 0.00,
    cash_in_hand   DECIMAL(10,2) DEFAULT 0.00,
    api_token      VARCHAR(64) UNIQUE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Expenses Table
CREATE TABLE expenses (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    user_id      INT NOT NULL,
    title        VARCHAR(150) NOT NULL,
    amount       DECIMAL(10,2) NOT NULL,
    category     VARCHAR(50) NOT NULL,
    expense_date DATE NOT NULL,
    source       ENUM('MANUAL','SMS_SYNC') DEFAULT 'MANUAL',
    upi_ref      VARCHAR(100),
    description  TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Cash Entries Table (Innovation: Cash Tracker)
CREATE TABLE cash_entries (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT NOT NULL,
    amount      DECIMAL(10,2) NOT NULL,
    category    VARCHAR(50) NOT NULL,
    entry_date  DATE NOT NULL,
    description VARCHAR(200),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cash_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Budgets Table (Innovation: Smart Budget Alerts & Spend Guard)
CREATE TABLE budgets (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    user_id      INT NOT NULL,
    category     VARCHAR(50) NOT NULL,
    limit_amount DECIMAL(10,2) NOT NULL,
    spent_amount DECIMAL(10,2) DEFAULT 0.00,
    start_date   DATE NOT NULL,
    end_date     DATE NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Goals Table (Innovation: Goal Motivation Engine)
CREATE TABLE goals (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    user_id       INT NOT NULL,
    title         VARCHAR(150) NOT NULL,
    description   TEXT,
    target_amount DECIMAL(10,2) NOT NULL,
    saved_amount  DECIMAL(10,2) DEFAULT 0.00,
    deadline      DATE NOT NULL,
    status        ENUM('IN_PROGRESS','ACHIEVED','FAILED') DEFAULT 'IN_PROGRESS',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 6. SMS Queue Table (Innovation: Real-Time SMS Sync)
CREATE TABLE sms_queue (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT NOT NULL,
    raw_sms         TEXT NOT NULL,
    parsed_amount   DECIMAL(10,2),
    parsed_merchant VARCHAR(150),
    upi_ref         VARCHAR(100),
    status          ENUM('PENDING','CONFIRMED','REJECTED') DEFAULT 'PENDING',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sms_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 7. Spending Personality Table (Innovation: Spending Personality Engine)
CREATE TABLE spending_personality (
    id                INT PRIMARY KEY AUTO_INCREMENT,
    user_id           INT NOT NULL,
    personality_type  VARCHAR(50) NOT NULL,
    primary_trait     VARCHAR(100),
    description       TEXT,
    calculation_month INT NOT NULL,
    calculation_year  INT NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_personality_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 8. System Settings Table
CREATE TABLE system_settings (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    setting_key   VARCHAR(100) UNIQUE NOT NULL,
    setting_value VARCHAR(255) NOT NULL,
    updated_by    INT,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_settings_user FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
);

-- 9. Audit Logs Table
CREATE TABLE audit_logs (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    admin_id    INT NOT NULL,
    action      VARCHAR(255) NOT NULL,
    target_user INT,
    timestamp   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Default Admin Account (password: admin123, BCrypt hash)
INSERT INTO users (name, email, password, role, status, monthly_income, cash_in_hand, api_token)
VALUES ('Admin', 'admin@myfinance.com', '$2a$12$XuJIR6cFP0sxRRZgcPlst.jM4R8/QhfO4OVUv50SwwboHAtiVNWXu', 'ADMIN', 'ACTIVE', 50000.00, 5000.00, 'admin-token-default-key-0001');

-- Default Platform System Settings
INSERT INTO system_settings (setting_key, setting_value) VALUES
('platform_name', 'MyFinance'),
('default_currency', 'INR'),
('session_timeout', '30'),
('max_login_attempts', '5'),
('budget_alerts_enabled', 'true'),
('email_reports_enabled', 'true'),
('sms_sync_interval_seconds', '30');
