SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS asset_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE asset_db;

-- Users Table
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL, -- In real app, should be hashed
    real_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL, -- ADMIN, EMPLOYEE
    department VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_user (username, password, real_name, role, department) VALUES 
('admin', '123456', '系统管理员', 'ADMIN', '行政部'),
('manager', '123456', '数据管理员', 'MANAGER', '财务部'),
('user', '123456', '普通员工', 'EMPLOYEE', '技术部'),
('dev1', '123456', '张三', 'EMPLOYEE', '研发部'),
('dev2', '123456', '李四', 'EMPLOYEE', '研发部');

-- Asset Categories
CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO category (name, description) VALUES 
('计算机', '笔记本, 台式机, 服务器'),
('打印机', '打印机, 扫描仪'),
('办公家具', '桌子, 椅子, 文件柜'),
('电子设备', '投影仪, 音响'),
('其他', '其他办公资产');

-- Assets Table
CREATE TABLE IF NOT EXISTS asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category_id BIGINT NOT NULL,
    model VARCHAR(100),
    serial_number VARCHAR(100),
    purchase_date DATE,
    supplier VARCHAR(100),
    price DECIMAL(10, 2),
    useful_life INT COMMENT 'Useful life in months',
    status VARCHAR(20) DEFAULT 'NORMAL', -- NORMAL, BORROWED, MAINTENANCE, SCRAPPED
    location VARCHAR(100),
    department_id VARCHAR(50), -- Owning department
    current_user_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO asset (asset_no, name, category_id, model, serial_number, purchase_date, price, status, location, department_id, current_user_id) VALUES
('AST-001', 'MacBook Pro', 1, 'M2 Pro', 'SN1001', '2024-01-01', 15000.00, 'NORMAL', 'A仓库', '技术部', NULL),
('AST-002', '戴尔显示器', 4, 'U2720Q', 'SN1002', '2024-01-05', 3000.00, 'BORROWED', 'A仓库', '技术部', 3),
('AST-003', '联想ThinkPad', 1, 'X1 Carbon', 'SN1003', '2023-12-01', 12000.00, 'MAINTENANCE', '维修中心', '研发部', NULL),
('AST-004', '惠普打印机', 2, 'LaserJet', 'SN1004', '2023-06-01', 2500.00, 'NORMAL', 'B仓库', '行政部', NULL),
('AST-005', '人体工学椅', 3, 'Herman Miller', 'SN1005', '2024-02-01', 8000.00, 'BORROWED', '研发区', '研发部', 4),
('AST-006', '投影仪', 5, 'Epson', 'SN1006', '2023-01-01', 5000.00, 'SCRAPPED', '报废仓', '行政部', NULL);

-- Borrow Records
CREATE TABLE IF NOT EXISTS borrow_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    borrow_date DATETIME NOT NULL,
    return_date DATETIME, -- Planned return date
    actual_return_date DATETIME,
    status VARCHAR(20) DEFAULT 'BORROWED', -- BORROWED, RETURNED
    remarks TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (asset_id) REFERENCES asset(id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO borrow_record (asset_id, user_id, borrow_date, return_date, actual_return_date, status, remarks) VALUES
(2, 3, '2024-03-01 09:00:00', '2024-04-01 09:00:00', NULL, 'BORROWED', '项目开发需要'),
(5, 4, '2024-03-05 10:00:00', NULL, NULL, 'BORROWED', '办公座椅申请'),
(1, 3, '2024-02-01 09:00:00', '2024-02-15 09:00:00', '2024-02-14 17:00:00', 'RETURNED', '临时借用测试');

-- Maintenance Records
CREATE TABLE IF NOT EXISTS maintenance_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    cost DECIMAL(10, 2),
    vendor VARCHAR(100),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, COMPLETED
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (asset_id) REFERENCES asset(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO maintenance_record (asset_id, description, cost, vendor, start_date, end_date, status) VALUES
(3, '屏幕闪烁，键盘失灵', NULL, '联想官方售后', '2024-03-10', NULL, 'PENDING'),
(6, '灯泡损坏，无法开机', 500.00, '爱普生维修点', '2023-11-01', '2023-11-05', 'COMPLETED');

-- Scrap Records
CREATE TABLE IF NOT EXISTS scrap_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    reason TEXT,
    approved_by BIGINT,
    scrap_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (asset_id) REFERENCES asset(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO scrap_record (asset_id, reason, approved_by, scrap_date, status) VALUES
(6, '维修成本过高，且已过保修期', 1, '2024-01-10', 'APPROVED');

-- Purchase Requests
CREATE TABLE IF NOT EXISTS purchase_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100),
    quantity INT DEFAULT 1,
    remarks TEXT,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    applicant_id BIGINT,
    applicant_name VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO purchase_request (category_name, quantity, remarks, status, applicant_id, applicant_name) VALUES
('高性能服务器', 2, '用于AI模型训练', 'PENDING', 4, '张三'),
('办公桌', 5, '新员工入职需要', 'APPROVED', 1, '系统管理员');
