-- 允许客户表的邮箱字段为空
-- 有些客户可能只有微信或WhatsApp，没有邮箱

ALTER TABLE `t_customer` 
MODIFY COLUMN `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱（可选）';

-- 更新索引说明：email 索引仍然保留，用于加速邮箱查询
-- 注意：如果有 UNIQUE 约束，需要先删除再重建（当前表结构没有 UNIQUE 约束）

