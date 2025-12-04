-- =====================================================
-- 迁移脚本：添加微信和WhatsApp相关字段
-- 执行时间：2025-12-03
-- 问题：t_customer表缺少wechat_name等字段
-- =====================================================

USE `truckTools`;

-- 添加微信名称字段
ALTER TABLE `t_customer` 
ADD COLUMN `wechat_name` VARCHAR(100) DEFAULT NULL COMMENT '微信名称/ID' 
AFTER `meeting_location`;

-- 添加微信二维码字段
ALTER TABLE `t_customer` 
ADD COLUMN `wechat_qrcode` VARCHAR(500) DEFAULT NULL COMMENT '微信二维码图片URL' 
AFTER `wechat_name`;

-- 添加WhatsApp名称字段
ALTER TABLE `t_customer` 
ADD COLUMN `whatsapp_name` VARCHAR(100) DEFAULT NULL COMMENT 'WhatsApp名称/号码' 
AFTER `wechat_qrcode`;

-- 添加WhatsApp二维码字段
ALTER TABLE `t_customer` 
ADD COLUMN `whatsapp_qrcode` VARCHAR(500) DEFAULT NULL COMMENT 'WhatsApp二维码图片URL' 
AFTER `whatsapp_name`;

