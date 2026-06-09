SET NAMES utf8mb4;

USE asset_db;

-- Inventory Task Table
CREATE TABLE IF NOT EXISTS inventory_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no VARCHAR(50) NOT NULL UNIQUE,
    task_name VARCHAR(100) NOT NULL,
    scope_type VARCHAR(20) NOT NULL COMMENT 'DEPARTMENT, WAREHOUSE, ALL',
    scope_value VARCHAR(100) COMMENT 'Department name or warehouse location',
    assignee_id BIGINT NOT NULL,
    assignee_name VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING, IN_PROGRESS, COMPLETED',
    total_count INT DEFAULT 0,
    checked_count INT DEFAULT 0,
    surplus_count INT DEFAULT 0,
    loss_count INT DEFAULT 0,
    difference_count INT DEFAULT 0,
    remarks TEXT,
    create_by BIGINT,
    create_name VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    start_time DATETIME,
    complete_time DATETIME,
    FOREIGN KEY (assignee_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Inventory Item Table
CREATE TABLE IF NOT EXISTS inventory_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    asset_id BIGINT COMMENT 'Book asset ID, null for surplus',
    asset_no VARCHAR(50) COMMENT 'Book asset number',
    asset_name VARCHAR(100),
    book_status VARCHAR(20) COMMENT 'Status from asset table',
    book_location VARCHAR(100),
    book_department VARCHAR(50),
    actual_status VARCHAR(20) COMMENT 'Actual asset condition: NORMAL, DAMAGED, LOST',
    actual_location VARCHAR(100),
    is_checked TINYINT(1) DEFAULT 0 COMMENT '0=pending, 1=checked',
    result_type VARCHAR(20) NOT NULL COMMENT 'Check result: MATCH, SURPLUS, LOSS, STATUS_DIFF, LOCATION_DIFF, BOTH_DIFF',
    remarks TEXT,
    check_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES inventory_task(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
