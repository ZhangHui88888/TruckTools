-- =====================================================
-- TruckTools 产品管理模块数据库表
-- 执行时间: 运行此脚本前请备份数据库
-- =====================================================

USE `truckTools`;

-- =====================================================
-- 1. 产品表
-- =====================================================
CREATE TABLE IF NOT EXISTS `t_product` (
    `id` BIGINT NOT NULL COMMENT '产品ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `brand_code` VARCHAR(20) DEFAULT NULL COMMENT '品牌缩写(MB/VL/SC等)',
    `brand_name` VARCHAR(100) DEFAULT NULL COMMENT '品牌全称',
    `xk_no` VARCHAR(50) NOT NULL COMMENT '内部编号',
    `oe_no` VARCHAR(100) NOT NULL COMMENT 'OE编号',
    `image_path` VARCHAR(500) DEFAULT NULL COMMENT '图片路径',
    `price_min` DECIMAL(10,2) DEFAULT NULL COMMENT '最低价(RMB)',
    `price_max` DECIMAL(10,2) DEFAULT NULL COMMENT '最高价(RMB)',
    `price_avg` DECIMAL(10,2) DEFAULT NULL COMMENT '平均价(RMB)',
    `remark` TEXT COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0=否, 1=是',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_oe_no` (`oe_no`),
    KEY `idx_brand_code` (`brand_code`),
    KEY `idx_xk_no` (`xk_no`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB COMMENT='产品表';

-- =====================================================
-- 2. 产品导入记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS `t_product_import` (
    `id` BIGINT NOT NULL COMMENT '导入ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `file_name` VARCHAR(200) NOT NULL COMMENT '文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
    `total_rows` INT DEFAULT 0 COMMENT '总行数',
    `success_count` INT DEFAULT 0 COMMENT '成功数量',
    `failed_count` INT DEFAULT 0 COMMENT '失败数量',
    `skipped_count` INT DEFAULT 0 COMMENT '跳过数量(重复)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=待处理, 1=处理中, 2=已完成, 3=失败',
    `error_message` TEXT COMMENT '错误信息',
    `started_at` DATETIME DEFAULT NULL COMMENT '开始处理时间',
    `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0=否, 1=是',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB COMMENT='产品导入记录表';

-- =====================================================
-- 3. 更新路线图
-- =====================================================
INSERT INTO `t_roadmap` (`id`, `module_name`, `module_key`, `description`, `features`, `planned_quarter`, `status`, `sort_order`) VALUES
(11, '产品管理', 'product', '产品目录、报价计算、Excel导入导出', '["产品目录", "Excel导入", "报价计算", "报价单导出"]', '2025 Q4', 'released', 3)
ON DUPLICATE KEY UPDATE 
    `module_name` = VALUES(`module_name`),
    `description` = VALUES(`description`),
    `features` = VALUES(`features`),
    `status` = VALUES(`status`);

