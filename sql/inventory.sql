USE asset_db;

CREATE TABLE IF NOT EXISTS inventory_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    department_id VARCHAR(50),
    location VARCHAR(100),
    assignee_id BIGINT NOT NULL,
    assignee_name VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING',
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(50),
    start_time DATETIME,
    end_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (assignee_id) REFERENCES sys_user(id),
    FOREIGN KEY (creator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    asset_id BIGINT,
    asset_no VARCHAR(50),
    asset_name VARCHAR(100),
    book_status VARCHAR(20),
    book_location VARCHAR(100),
    book_department VARCHAR(50),
    actual_status VARCHAR(20),
    actual_location VARCHAR(100),
    result VARCHAR(20),
    remarks TEXT,
    check_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES inventory_task(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
