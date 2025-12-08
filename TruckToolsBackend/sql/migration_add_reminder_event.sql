-- 添加提醒事件相关字段
-- 执行时间: 2024-12

-- 添加事件类型字段
ALTER TABLE t_customer_event ADD COLUMN event_type VARCHAR(20) DEFAULT 'normal' COMMENT '事件类型: normal=普通事件, reminder=提醒事件';

-- 添加提醒时间字段
ALTER TABLE t_customer_event ADD COLUMN reminder_time DATETIME DEFAULT NULL COMMENT '提醒时间（仅提醒事件有效）';

-- 添加提醒触发状态字段
ALTER TABLE t_customer_event ADD COLUMN reminder_triggered TINYINT DEFAULT 0 COMMENT '提醒是否已触发: 0=未触发, 1=已触发';

-- 添加索引以便快速查询未触发的提醒
CREATE INDEX idx_customer_event_reminder ON t_customer_event(event_type, reminder_triggered, reminder_time);
