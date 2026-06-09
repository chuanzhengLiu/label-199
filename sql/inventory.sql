SET NAMES utf8mb4;

USE asset_db;

-- 盘点任务表
CREATE TABLE IF NOT EXISTS inventory_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no VARCHAR(50) NOT NULL UNIQUE COMMENT '盘点任务编号',
    name VARCHAR(100) NOT NULL COMMENT '任务名称',
    scope_type VARCHAR(20) NOT NULL COMMENT '盘点范围类型: DEPARTMENT-按部门, LOCATION-按仓库',
    scope_value VARCHAR(100) NOT NULL COMMENT '范围值: 部门名称或仓库名称',
    assignee_id BIGINT COMMENT '执行人ID',
    assignee_name VARCHAR(50) COMMENT '执行人姓名',
    creator_id BIGINT COMMENT '创建人ID',
    creator_name VARCHAR(50) COMMENT '创建人姓名',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING-待执行, IN_PROGRESS-执行中, COMPLETED-已完成',
    total_count INT DEFAULT 0 COMMENT '账面应盘总数',
    checked_count INT DEFAULT 0 COMMENT '已盘点数',
    normal_count INT DEFAULT 0 COMMENT '账实相符数',
    profit_count INT DEFAULT 0 COMMENT '盘盈数',
    loss_count INT DEFAULT 0 COMMENT '盘亏数',
    remark TEXT COMMENT '任务备注',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '完成时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 盘点明细表（记录每个资产的盘点结果，仅作为盘点记录，不修改资产原始台账）
CREATE TABLE IF NOT EXISTS inventory_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '盘点任务ID',
    asset_id BIGINT COMMENT '资产ID（盘盈记录可为空）',
    asset_no VARCHAR(50) COMMENT '资产编号',
    asset_name VARCHAR(100) COMMENT '资产名称',
    book_status VARCHAR(20) COMMENT '账面状态',
    book_location VARCHAR(100) COMMENT '账面位置',
    book_department VARCHAR(50) COMMENT '账面部门',
    actual_status VARCHAR(20) COMMENT '实物状态',
    actual_location VARCHAR(100) COMMENT '实物位置',
    result VARCHAR(20) DEFAULT 'UNCHECKED' COMMENT 'UNCHECKED-未盘, NORMAL-账实相符, PROFIT-盘盈, LOSS-盘亏',
    remark TEXT COMMENT '差异备注',
    checker_id BIGINT COMMENT '盘点人ID',
    check_time DATETIME COMMENT '盘点时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task_id (task_id),
    INDEX idx_asset_id (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
