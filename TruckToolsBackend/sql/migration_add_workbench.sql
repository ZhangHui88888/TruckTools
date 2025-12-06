-- =====================================================
-- 迁移脚本：添加工作台相关字段
-- 执行时间：2025-12-06
-- 功能：支持工作台事件跟进管理
-- =====================================================

USE `truckTools`;

-- 1. 扩展客户事件表字段
ALTER TABLE `t_customer_event`
    ADD COLUMN `is_system_generated` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统自动生成: 0=否, 1=是' AFTER `event_status`,
    ADD COLUMN `parent_event_id` BIGINT DEFAULT NULL COMMENT '关联的父事件ID' AFTER `is_system_generated`,
    ADD COLUMN `attachment_urls` TEXT DEFAULT NULL COMMENT '附件URL列表(JSON格式)' AFTER `parent_event_id`;

-- 添加父事件索引
ALTER TABLE `t_customer_event`
    ADD KEY `idx_parent_event_id` (`parent_event_id`);

-- 2. 扩展客户表字段
ALTER TABLE `t_customer`
    ADD COLUMN `stop_follow_up` TINYINT NOT NULL DEFAULT 0 COMMENT '是否停止跟进: 0=否, 1=是' AFTER `follow_up_status`,
    ADD COLUMN `stop_follow_up_time` DATETIME DEFAULT NULL COMMENT '停止跟进时间' AFTER `stop_follow_up`,
    ADD COLUMN `stop_follow_up_reason` VARCHAR(500) DEFAULT NULL COMMENT '停止跟进原因' AFTER `stop_follow_up_time`,
    ADD COLUMN `last_event_time` DATETIME DEFAULT NULL COMMENT '最后事件时间' AFTER `stop_follow_up_reason`,
    ADD COLUMN `pending_event_count` INT NOT NULL DEFAULT 0 COMMENT '待处理事件数量' AFTER `last_event_time`;

-- 添加索引
ALTER TABLE `t_customer`
    ADD KEY `idx_stop_follow_up` (`stop_follow_up`),
    ADD KEY `idx_last_event_time` (`last_event_time`),
    ADD KEY `idx_pending_event_count` (`pending_event_count`);

-- 3. 初始化现有客户的last_event_time字段
UPDATE `t_customer` c
SET c.last_event_time = (
    SELECT MAX(e.event_time)
    FROM `t_customer_event` e
    WHERE e.customer_id = c.id AND e.deleted = 0
)
WHERE EXISTS (
    SELECT 1 FROM `t_customer_event` e
    WHERE e.customer_id = c.id AND e.deleted = 0
);

-- 4. 初始化现有客户的pending_event_count字段
UPDATE `t_customer` c
SET c.pending_event_count = (
    SELECT COUNT(*)
    FROM `t_customer_event` e
    WHERE e.customer_id = c.id 
      AND e.event_status = 'pending_us' 
      AND e.deleted = 0
);
