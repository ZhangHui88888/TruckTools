-- 为历史已发送邮件创建客户事件
-- 执行时间: 2024-12-06
-- 说明: 为所有状态为"发送成功"(status=2)的邮件日志创建对应的客户事件
-- 注意: 每个客户只保留最新的一封邮件事件，避免工作台显示过多历史记录

-- 先删除之前可能插入的邮件事件（如果需要重新执行）
-- DELETE FROM t_customer_event WHERE event_content LIKE '已发送邮件:%' AND is_system_generated = 1;

-- 为每个客户只插入最新一封邮件的事件
INSERT INTO t_customer_event (
    id,
    user_id,
    customer_id,
    event_time,
    event_content,
    event_status,
    is_system_generated,
    created_at,
    updated_at,
    deleted
)
SELECT 
    -- 使用时间戳+客户ID生成唯一ID
    FLOOR(UNIX_TIMESTAMP(NOW(6)) * 1000000) + el.customer_id,
    el.user_id,
    el.customer_id,
    el.sent_at,
    CONCAT('已发送邮件: ', el.subject),
    'pending_customer',
    1,
    el.sent_at,  -- created_at 使用发送时间
    NOW(),
    0
FROM t_email_log el
INNER JOIN (
    -- 获取每个客户最新发送成功的邮件
    SELECT customer_id, MAX(sent_at) as max_sent_at
    FROM t_email_log
    WHERE status = 2 AND customer_id IS NOT NULL AND sent_at IS NOT NULL
    GROUP BY customer_id
) latest ON el.customer_id = latest.customer_id AND el.sent_at = latest.max_sent_at
WHERE el.status = 2
  AND el.customer_id IS NOT NULL
  AND NOT EXISTS (
    -- 避免重复插入
    SELECT 1 FROM t_customer_event ce 
    WHERE ce.customer_id = el.customer_id 
      AND ce.event_content LIKE '已发送邮件:%'
  );

-- 更新客户的跟进状态和最后事件时间
UPDATE t_customer c
SET 
    follow_up_status = 'pending_customer',
    last_event_time = (
        SELECT MAX(sent_at)
        FROM t_email_log el
        WHERE el.customer_id = c.id AND el.status = 2 AND el.sent_at IS NOT NULL
    )
WHERE EXISTS (
    SELECT 1 FROM t_email_log el 
    WHERE el.customer_id = c.id AND el.status = 2
)
AND (c.follow_up_status IS NULL OR c.follow_up_status = '');

-- 查看插入的事件数量
SELECT COUNT(*) AS '新增邮件事件数' FROM t_customer_event WHERE event_content LIKE '已发送邮件:%';
