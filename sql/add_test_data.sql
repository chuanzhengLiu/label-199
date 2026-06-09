USE asset_db;

-- 1. 新增用户数据
INSERT INTO sys_user (username, password, real_name, role, department) VALUES 
('zhangsan', '123456', '张三', 'EMPLOYEE', '市场部'),
('lisi', '123456', '李四', 'EMPLOYEE', '人事部');

-- 2. 新增资产分类
INSERT INTO category (name, description) VALUES 
('通讯设备', '手机, 对讲机, 电话会议设备'),
('测试设备', '测试机, 平板, 扫码枪');

-- 3. 新增资产数据
-- 注意：这里假设 category_id 分别对应 6 和 7，以及复用原有的分类
INSERT INTO asset (asset_no, name, category_id, model, serial_number, purchase_date, price, status, location, department_id, current_user_id) VALUES
('AST-003', 'iPhone 15', 6, '128G', 'SN_IP15_001', '2024-02-01', 5999.00, 'NORMAL', 'B仓库', '市场部', NULL),
('AST-004', 'iPad Pro', 7, '11 inch', 'SN_IPAD_001', '2024-02-15', 6500.00, 'BORROWED', '个人持有', '技术部', 4), -- 借用给张三(id=4)
('AST-005', '佳能打印机', 2, 'LBP6018w', 'SN_PR_001', '2023-12-01', 1200.00, 'MAINTENANCE', '维修中心', '行政部', NULL),
('AST-006', '旧办公桌', 3, '木质1.2米', 'SN_DK_001', '2020-01-01', 500.00, 'SCRAPPED', '废品库', '行政部', NULL);

-- 4. 新增借用记录
-- 记录1: 张三借用 iPad Pro (当前状态为借用中)
INSERT INTO borrow_record (asset_id, user_id, borrow_date, return_date, status, remarks) VALUES 
(4, 4, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'BORROWED', '项目测试需要');

-- 记录2: 李四借用 iPhone 15 后已归还 (历史记录)
INSERT INTO borrow_record (asset_id, user_id, borrow_date, return_date, actual_return_date, status, remarks) VALUES 
(3, 5, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 'RETURNED', '临时外出使用');

-- 5. 新增维修记录
-- 记录1: 打印机正在维修
INSERT INTO maintenance_record (asset_id, description, cost, vendor, start_date, status) VALUES 
(5, '卡纸，无法进纸', 200.00, '佳能官方售后', DATE_SUB(NOW(), INTERVAL 2 DAY), 'PENDING');

-- 记录2: MacBook Pro (AST-001) 之前的维修记录 (已完成)
INSERT INTO maintenance_record (asset_id, description, cost, vendor, start_date, end_date, status) VALUES 
(1, '屏幕更换', 3000.00, '苹果授权维修', '2024-01-10', '2024-01-15', 'COMPLETED');

-- 6. 新增报废记录
-- 记录1: 旧办公桌申请报废 (待审批)
INSERT INTO scrap_record (asset_id, reason, scrap_date, status) VALUES 
(6, '年久失修，桌腿断裂', NOW(), 'PENDING');

-- 记录2: 之前的某个设备报废记录 (已批准 - 假设有一个虚拟资产或复用AST-006作为示例，这里为了逻辑严谨，我们仅添加一条PENDING的，因为AST-006状态已经是SCRAPPED，理应是APPROVED，修改状态为APPROVED)
UPDATE scrap_record SET status = 'APPROVED', approved_by = 1 WHERE asset_id = 6;
-- 再加一条待审批的，比如 AST-002 显示器
INSERT INTO scrap_record (asset_id, reason, scrap_date, status) VALUES 
(2, '屏幕出现坏点，影响使用', NOW(), 'PENDING');


-- 7. 新增采购申请记录
INSERT INTO purchase_request (category_name, quantity, remarks, status, applicant_id, applicant_name) VALUES 
('高性能笔记本', 5, '新员工入职需求', 'PENDING', 2, '数据管理员'),
('人体工学椅', 10, '替换损坏座椅', 'APPROVED', 1, '系统管理员');
