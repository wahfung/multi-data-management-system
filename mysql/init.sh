#!/bin/bash
mysql -u root -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 <<EOF
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS data_source_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE data_source_manager;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ds_vendor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_code VARCHAR(50) NOT NULL,
    vendor_name VARCHAR(100) NOT NULL,
    api_base_url VARCHAR(500) NOT NULL,
    api_version VARCHAR(20),
    auth_type VARCHAR(20) DEFAULT 'none',
    auth_config TEXT,
    adapter_type VARCHAR(50) DEFAULT 'default',
    description TEXT,
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_vendor_code (vendor_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ds_sync_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_id BIGINT NOT NULL,
    vendor_name VARCHAR(100),
    operation_type VARCHAR(20) NOT NULL,
    request_url VARCHAR(500),
    request_method VARCHAR(10),
    request_headers TEXT,
    request_body MEDIUMTEXT,
    response_code INT,
    response_body MEDIUMTEXT,
    execution_time BIGINT,
    sync_status TINYINT DEFAULT 0,
    error_message TEXT,
    data_count INT DEFAULT 0,
    operator_name VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_vendor_id (vendor_id),
    INDEX idx_create_time (create_time),
    INDEX idx_sync_status (sync_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ds_sync_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_id BIGINT NOT NULL,
    record_id BIGINT,
    data_type VARCHAR(50),
    source_id VARCHAR(100),
    raw_data MEDIUMTEXT,
    normalized_data MEDIUMTEXT,
    process_status TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_vendor_id (vendor_id),
    INDEX idx_record_id (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_user (username, password, nickname, status, deleted) VALUES
('admin', '\$2a\$10\$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', 1, 0)
ON DUPLICATE KEY UPDATE password = '\$2a\$10\$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2';

INSERT INTO ds_vendor (vendor_code, vendor_name, api_base_url, api_version, auth_type, auth_config, adapter_type, description, status, deleted) VALUES
('JSONPLACEHOLDER', 'JSONPlaceholder', 'https://jsonplaceholder.typicode.com', 'v1', 'none', NULL, 'rest_api', 'Free REST API for testing - posts/users/comments', 1, 0),
('REQRES', 'ReqRes', 'https://reqres.in/api', 'v1', 'none', NULL, 'rest_api', 'Free REST API for testing - users/register/login', 1, 0),
('HTTPBIN', 'HTTPBin', 'https://httpbin.org', 'v1', 'none', NULL, 'rest_api', 'HTTP Request/Response Service', 1, 0),
('DUMMYJSON', 'DummyJSON', 'https://dummyjson.com', 'v1', 'none', NULL, 'rest_api', 'Fake REST API with products/users/carts', 1, 0)
ON DUPLICATE KEY UPDATE api_base_url = VALUES(api_base_url);
EOF
