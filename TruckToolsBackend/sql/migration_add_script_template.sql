-- 创建话术模板表
-- 执行时间: 2024-12

CREATE TABLE IF NOT EXISTS t_script_template (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    content TEXT NOT NULL COMMENT '模板内容，支持变量如 {{name}}, {{company}} 等',
    description VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用: 0=禁用, 1=启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0=否, 1=是',
    INDEX idx_user_id (user_id),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话术模板表';

-- 插入示例模板
INSERT INTO t_script_template (id, user_id, name, content, description, sort_order, enabled) VALUES
(1, 1, '初次联系', '您好 {{name}}，我是XX公司的销售代表。很高兴在{{meetingLocation}}与您相识。我们公司专注于卡车配件领域，希望有机会与{{company}}建立合作关系。', '用于首次微信联系客户', 1, 1),
(2, 1, '展会后跟进', '{{name}}您好，感谢您在{{meetingTime}}{{meetingLocation}}展会上抽出宝贵时间与我们交流。针对您提到的需求，我们已经整理了相关产品资料，方便时可以详细沟通。', '展会结束后的跟进话术', 2, 1),
(3, 1, '报价跟进', '{{name}}您好，之前发送给您的报价单不知道您是否有时间查看？如有任何疑问或需要调整的地方，请随时告诉我。期待与{{company}}的合作！', '发送报价后的跟进', 3, 1);
