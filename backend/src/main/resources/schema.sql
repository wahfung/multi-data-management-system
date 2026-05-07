-- 数据源管理系统数据库初始化脚本
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS data_source_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE data_source_manager;

SET NAMES utf8mb4;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 数据源厂商表
CREATE TABLE IF NOT EXISTS ds_vendor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    vendor_code VARCHAR(50) NOT NULL COMMENT '厂商编码',
    vendor_name VARCHAR(100) NOT NULL COMMENT '厂商名称',
    api_base_url VARCHAR(500) NOT NULL COMMENT 'API基础地址',
    api_version VARCHAR(20) COMMENT 'API版本',
    auth_type VARCHAR(20) DEFAULT 'none' COMMENT '认证类型: none/api_key/basic/bearer/oauth2',
    auth_config TEXT COMMENT '认证配置(JSON格式)',
    adapter_type VARCHAR(50) DEFAULT 'default' COMMENT '适配器类型',
    description TEXT COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_vendor_code (vendor_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据源厂商表';

-- 数据同步记录表
CREATE TABLE IF NOT EXISTS ds_sync_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    vendor_id BIGINT NOT NULL COMMENT '厂商ID',
    vendor_name VARCHAR(100) COMMENT '厂商名称',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型: pull/push/sync/query/test',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_headers TEXT COMMENT '请求头(JSON)',
    request_body MEDIUMTEXT COMMENT '请求体(JSON)',
    response_code INT COMMENT '响应状态码',
    response_body MEDIUMTEXT COMMENT '响应体(JSON)',
    execution_time BIGINT COMMENT '执行耗时(毫秒)',
    sync_status TINYINT DEFAULT 0 COMMENT '同步状态: 0-待处理, 1-成功, 2-失败, 3-部分成功',
    error_message TEXT COMMENT '错误信息',
    data_count INT DEFAULT 0 COMMENT '数据条数',
    operator_name VARCHAR(50) COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_vendor_id (vendor_id),
    INDEX idx_create_time (create_time),
    INDEX idx_sync_status (sync_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据同步记录表';

-- 同步数据表
CREATE TABLE IF NOT EXISTS ds_sync_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    vendor_id BIGINT NOT NULL COMMENT '厂商ID',
    record_id BIGINT COMMENT '同步记录ID',
    data_type VARCHAR(50) COMMENT '数据类型',
    source_id VARCHAR(100) COMMENT '源数据ID',
    raw_data MEDIUMTEXT COMMENT '原始数据(JSON)',
    normalized_data MEDIUMTEXT COMMENT '标准化数据(JSON)',
    process_status TINYINT DEFAULT 0 COMMENT '处理状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_vendor_id (vendor_id),
    INDEX idx_record_id (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步数据表';

-- 初始化管理员账号 (密码: admin123, BCrypt加密)
INSERT INTO sys_user (username, password, nickname, status, deleted) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', 1, 0)
ON DUPLICATE KEY UPDATE password = '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2';

-- 初始化示例数据源厂商 (使用公开可用的测试API)
INSERT INTO ds_vendor (vendor_code, vendor_name, api_base_url, api_version, auth_type, auth_config, adapter_type, description, status, deleted) VALUES
('JSONPLACEHOLDER', 'JSONPlaceholder', 'https://jsonplaceholder.typicode.com', 'v1', 'none', NULL, 'rest_api', 'Free REST API - posts/users/comments', 1, 0),
('REQRES', 'ReqRes', 'https://reqres.in/api', 'v1', 'none', NULL, 'rest_api', 'Free REST API - users/register/login', 1, 0),
('HTTPBIN', 'HTTPBin', 'https://httpbin.org', 'v1', 'none', NULL, 'rest_api', 'HTTP Request/Response Service', 1, 0),
('DUMMYJSON', 'DummyJSON', 'https://dummyjson.com', 'v1', 'none', NULL, 'rest_api', 'Fake REST API - products/users/carts', 1, 0)
ON DUPLICATE KEY UPDATE api_base_url = VALUES(api_base_url);
