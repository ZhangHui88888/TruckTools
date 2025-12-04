-- =====================================================
-- 迁移脚本：添加跟进状态字段
-- 执行时间：2025-12-04
-- 功能：添加客户跟进状态字段
-- =====================================================

USE `truckTools`;

-- 添加跟进状态字段
-- pending_customer: 等待客户回复（绿色）
-- pending_us: 等待我们回复/报价（黄色）
-- completed: 已完成（绿色）
ALTER TABLE `t_customer` 
ADD COLUMN `follow_up_status` VARCHAR(20) DEFAULT 'pending_us' COMMENT '跟进状态：pending_customer=等待客户回复, pending_us=等待我们回复, completed=已完成' 
AFTER `whatsapp_qrcode`;

