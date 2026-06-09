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
