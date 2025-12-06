-- =====================================================
-- 迁移脚本：添加客户事件表
-- 执行时间：2025-12-04
-- 功能：添加客户事件跟踪系统
-- =====================================================

USE `truckTools`;

-- 创建客户事件表
CREATE TABLE IF NOT EXISTS `t_customer_event` (
    `id` BIGINT NOT NULL COMMENT '事件ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `event_time` DATETIME NOT NULL COMMENT '事件时间',
    `event_location` VARCHAR(200) DEFAULT NULL COMMENT '事件地点',
    `event_content` TEXT NOT NULL COMMENT '事件内容',
    `event_status` VARCHAR(20) NOT NULL DEFAULT 'pending_us' COMMENT '事件进度状态: pending_customer=等待客户回复, pending_us=等待我们回复',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0=否, 1=是',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_event_time` (`event_time`),
    KEY `idx_event_status` (`event_status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB COMMENT='客户事件表';

