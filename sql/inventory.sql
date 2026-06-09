SET NAMES utf8mb4;

USE asset_db;

-- Inventory Task Table
CREATE TABLE IF NOT EXISTS inventory_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(100) NOT NULL,
    task_type VARCHAR(20) DEFAULT 'DEPARTMENT', -- DEPARTMENT, LOCATION, ALL
    target_value VARCHAR(100), -- department name or location
    executor_id BIGINT NOT NULL,
    executor_name VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED
    total_count INT DEFAULT 0,
    checked_count INT DEFAULT 0,
    profit_count INT DEFAULT 0,
    loss_count INT DEFAULT 0,
    start_time DATETIME,
    end_time DATETIME,
    creator_id BIGINT,
    creator_name VARCHAR(50),
    remark TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (executor_id) REFERENCES sys_user(id),
    FOREIGN KEY (creator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Inventory Detail Table
CREATE TABLE IF NOT EXISTS inventory_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    asset_id BIGINT,
    asset_no VARCHAR(50),
    asset_name VARCHAR(100),
    book_status VARCHAR(20), -- 账面状态
    book_location VARCHAR(100), -- 账面存放地点
    book_department_id VARCHAR(50), -- 账面所属部门
    actual_status VARCHAR(20), -- 实际状态
    actual_location VARCHAR(100), -- 实际存放地点
    check_result VARCHAR(20), -- NORMAL, PROFIT, LOSS, STATUS_DIFF
    is_checked TINYINT DEFAULT 0,
    check_time DATETIME,
    checker_id BIGINT,
    checker_name VARCHAR(50),
    remark TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES inventory_task(id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES asset(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
