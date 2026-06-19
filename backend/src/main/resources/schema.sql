-- 创建数据库
CREATE DATABASE IF NOT EXISTS apartment_deposit DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE apartment_deposit;

-- ============================================
-- 用户表
-- ============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码: MERCHANT-招商主管, RESIDENT-住户, FINANCE-财务',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ============================================
-- 房间表
-- ============================================
DROP TABLE IF EXISTS apt_room;
CREATE TABLE apt_room (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    room_no VARCHAR(50) NOT NULL COMMENT '房间编号',
    building VARCHAR(50) NOT NULL COMMENT '楼栋',
    floor INT NOT NULL COMMENT '楼层',
    room_type VARCHAR(50) COMMENT '房型: 一室一厅, 两室一厅等',
    area DECIMAL(10,2) COMMENT '面积(平方米)',
    monthly_rent DECIMAL(12,2) NOT NULL COMMENT '月租金',
    deposit_amount DECIMAL(12,2) NOT NULL COMMENT '押金金额',
    status VARCHAR(20) DEFAULT 'VACANT' COMMENT '状态: VACANT-空置, OCCUPIED-已入住, MAINTENANCE-维修中',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_room_no (room_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间信息表';

-- ============================================
-- 入住单表
-- ============================================
DROP TABLE IF EXISTS apt_checkin;
CREATE TABLE apt_checkin (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    checkin_no VARCHAR(50) NOT NULL COMMENT '入住单号',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    resident_id BIGINT NOT NULL COMMENT '住户用户ID',
    merchant_id BIGINT NOT NULL COMMENT '招商主管用户ID',
    checkin_date DATE NOT NULL COMMENT '计划入住日期',
    checkout_date DATE COMMENT '计划退房日期',
    actual_checkin_date DATE COMMENT '实际入住日期',
    actual_checkout_date DATE COMMENT '实际退房日期',
    monthly_rent DECIMAL(12,2) NOT NULL COMMENT '月租金',
    deposit_amount DECIMAL(12,2) NOT NULL COMMENT '押金金额',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿, PENDING_HANDOVER-待交接, HANDED_OVER-已交接, CHECKED_IN-已入住, SETTLING-结算中, SETTLED-已结算, CANCELLED-已取消',
    handover_confirmed TINYINT DEFAULT 0 COMMENT '房间交接是否确认: 0-未确认, 1-已确认',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_checkin_no (checkin_no),
    KEY idx_room_id (room_id),
    KEY idx_resident_id (resident_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入住单表';

-- ============================================
-- 房间交接表
-- ============================================
DROP TABLE IF EXISTS apt_handover;
CREATE TABLE apt_handover (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    checkin_id BIGINT NOT NULL COMMENT '入住单ID',
    handover_type VARCHAR(20) NOT NULL COMMENT '交接类型: CHECKIN-入住交接, CHECKOUT-退房交接',
    handover_time DATETIME COMMENT '交接时间',
    resident_id BIGINT NOT NULL COMMENT '住户ID',
    merchant_id BIGINT NOT NULL COMMENT '招商主管ID',
    meter_water DECIMAL(10,2) COMMENT '水表读数',
    meter_electric DECIMAL(10,2) COMMENT '电表读数',
    meter_gas DECIMAL(10,2) COMMENT '燃气表读数',
    key_count INT DEFAULT 0 COMMENT '钥匙数量',
    door_card_count INT DEFAULT 0 COMMENT '门禁卡数量',
    furniture_condition TEXT COMMENT '家具设施情况',
    appliance_condition TEXT COMMENT '家电设施情况',
    other_condition TEXT COMMENT '其他情况说明',
    resident_sign TINYINT DEFAULT 0 COMMENT '住户签字确认: 0-未确认, 1-已确认',
    merchant_sign TINYINT DEFAULT 0 COMMENT '招商主管签字确认: 0-未确认, 1-已确认',
    confirmed TINYINT DEFAULT 0 COMMENT '交接是否完成确认: 0-未完成, 1-已完成',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_checkin_id (checkin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间交接表';

-- ============================================
-- 押金记录表
-- ============================================
DROP TABLE IF EXISTS apt_deposit;
CREATE TABLE apt_deposit (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    deposit_no VARCHAR(50) NOT NULL COMMENT '押金单号',
    checkin_id BIGINT NOT NULL COMMENT '入住单ID',
    resident_id BIGINT NOT NULL COMMENT '住户ID',
    finance_id BIGINT COMMENT '财务人员ID',
    trans_type VARCHAR(20) NOT NULL COMMENT '交易类型: COLLECT-收取, REFUND-退还',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    pay_method VARCHAR(20) COMMENT '支付方式: CASH-现金, BANK-银行转账, ALIPAY-支付宝, WECHAT-微信',
    pay_time DATETIME COMMENT '支付时间',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-待处理, COMPLETED-已完成, CANCELLED-已取消',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_deposit_no (deposit_no),
    KEY idx_checkin_id (checkin_id),
    KEY idx_resident_id (resident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='押金记录表';

-- ============================================
-- 扣款明细表
-- ============================================
DROP TABLE IF EXISTS apt_deduction;
CREATE TABLE apt_deduction (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    checkin_id BIGINT NOT NULL COMMENT '入住单ID',
    deduction_type VARCHAR(50) NOT NULL COMMENT '扣款类型: DAMAGE-物品损坏, CLEAN-清洁费, UTILITY-水电费, OVERDUE-逾期费用, OTHER-其他',
    deduction_name VARCHAR(100) NOT NULL COMMENT '扣款项目名称',
    amount DECIMAL(12,2) NOT NULL COMMENT '扣款金额',
    description TEXT COMMENT '扣款说明',
    pic_urls VARCHAR(1000) COMMENT '证明图片URL(多个逗号分隔)',
    operator_id BIGINT COMMENT '操作人ID',
    locked TINYINT DEFAULT 0 COMMENT '是否锁定: 0-未锁定(可编辑), 1-已锁定(不可编辑)',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_checkin_id (checkin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='押金扣款明细表';

-- ============================================
-- 退租结算表
-- ============================================
DROP TABLE IF EXISTS apt_settlement;
CREATE TABLE apt_settlement (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    settlement_no VARCHAR(50) NOT NULL COMMENT '结算单号',
    checkin_id BIGINT NOT NULL COMMENT '入住单ID',
    resident_id BIGINT NOT NULL COMMENT '住户ID',
    finance_id BIGINT COMMENT '财务人员ID',
    merchant_id BIGINT COMMENT '招商主管ID',
    actual_checkout_date DATE NOT NULL COMMENT '实际退房日期',
    total_rent DECIMAL(12,2) DEFAULT 0 COMMENT '应缴租金合计',
    paid_rent DECIMAL(12,2) DEFAULT 0 COMMENT '已缴租金合计',
    remaining_rent DECIMAL(12,2) DEFAULT 0 COMMENT '欠缴租金',
    utility_fee DECIMAL(12,2) DEFAULT 0 COMMENT '水电燃气等费用',
    total_deposit DECIMAL(12,2) DEFAULT 0 COMMENT '押金总额',
    total_deduction DECIMAL(12,2) DEFAULT 0 COMMENT '扣款总额',
    refund_amount DECIMAL(12,2) DEFAULT 0 COMMENT '应退金额',
    payable_amount DECIMAL(12,2) DEFAULT 0 COMMENT '应补金额',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿, CONFIRMED-已确认, COMPLETED-已完成, CANCELLED-已取消',
    settlement_time DATETIME COMMENT '结算完成时间',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_settlement_no (settlement_no),
    KEY idx_checkin_id (checkin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退租结算表';

-- ============================================
-- 费用明细表
-- ============================================
DROP TABLE IF EXISTS apt_fee_item;
CREATE TABLE apt_fee_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    checkin_id BIGINT NOT NULL COMMENT '入住单ID',
    fee_type VARCHAR(20) NOT NULL COMMENT '费用类型: RENT-租金, WATER-水费, ELECTRIC-电费, GAS-燃气费, OTHER-其他',
    fee_name VARCHAR(100) NOT NULL COMMENT '费用名称',
    fee_month VARCHAR(10) COMMENT '费用月份: yyyy-MM',
    amount DECIMAL(12,2) NOT NULL COMMENT '费用金额',
    paid TINYINT DEFAULT 0 COMMENT '是否已缴: 0-未缴, 1-已缴',
    paid_time DATETIME COMMENT '缴费时间',
    operator_id BIGINT COMMENT '操作人ID',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_checkin_id (checkin_id),
    KEY idx_fee_month (fee_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用明细表';

-- ============================================
-- 退租争议表
-- ============================================
DROP TABLE IF EXISTS apt_dispute;
CREATE TABLE apt_dispute (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    checkin_id BIGINT NOT NULL COMMENT '入住单ID',
    deduction_id BIGINT COMMENT '关联扣款ID',
    resident_id BIGINT NOT NULL COMMENT '住户ID',
    dispute_type VARCHAR(50) NOT NULL COMMENT '争议类型: DEDUCTION-扣款争议, FEE-费用争议, DAMAGE-损坏争议, OTHER-其他',
    content TEXT NOT NULL COMMENT '争议内容',
    evidence_urls VARCHAR(2000) COMMENT '证据图片URL(多个逗号分隔)',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-待审核, REVIEWING-审核中, APPROVED-已通过, REJECTED-已驳回',
    reviewer_id BIGINT COMMENT '审核人ID',
    review_opinion TEXT COMMENT '审核意见',
    adjusted_amount DECIMAL(12,2) COMMENT '调整后金额',
    review_time DATETIME COMMENT '审核时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_checkin_id (checkin_id),
    KEY idx_deduction_id (deduction_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退租争议表';

-- ============================================
-- 初始化数据
-- ============================================

-- 初始用户 (密码均为 123456, BCrypt加密)
INSERT INTO sys_user (username, password, real_name, phone, email, role_code, status) VALUES
('merchant01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', '张招商', '13800000001', 'merchant01@test.com', 'MERCHANT', 1),
('resident01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', '李住户', '13800000002', 'resident01@test.com', 'RESIDENT', 1),
('resident02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', '王住户', '13800000003', 'resident02@test.com', 'RESIDENT', 1),
('finance01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', '赵财务', '13800000004', 'finance01@test.com', 'FINANCE', 1);

-- 初始房间
INSERT INTO apt_room (room_no, building, floor, room_type, area, monthly_rent, deposit_amount, status, remark) VALUES
('A101', 'A栋', 1, '一室一厅', 45.00, 2000.00, 4000.00, 'VACANT', '朝南,精装修'),
('A102', 'A栋', 1, '两室一厅', 65.00, 2800.00, 5600.00, 'VACANT', '朝南,拎包入住'),
('A201', 'A栋', 2, '一室一厅', 45.00, 2000.00, 4000.00, 'VACANT', '朝北'),
('A202', 'A栋', 2, '两室一厅', 65.00, 2800.00, 5600.00, 'VACANT', '南北通透'),
('B101', 'B栋', 1, '一室一厅', 48.00, 2200.00, 4400.00, 'VACANT', '独立卫浴'),
('B102', 'B栋', 1, '三室一厅', 90.00, 3600.00, 7200.00, 'VACANT', '适合家庭入住');
