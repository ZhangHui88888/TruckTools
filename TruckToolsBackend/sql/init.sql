-- =====================================================
-- TruckTools 数据库建表脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `truckTools` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;

USE `truckTools`;

-- =====================================================
-- 1. 用户与认证相关表
-- =====================================================

-- 用户表
CREATE TABLE IF NOT EXISTS `t_user` (
    `id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=正常',
    `email_verified` TINYINT NOT NULL DEFAULT 0 COMMENT '邮箱是否验证: 0=未验证, 1=已验证',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0=否, 1=是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB COMMENT='用户表';

-- 用户SMTP配置表
CREATE TABLE IF NOT EXISTS `t_user_smtp_config` (
    `id` BIGINT NOT NULL COMMENT '配置ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `smtp_host` VARCHAR(100) NOT NULL COMMENT 'SMTP服务器地址',
    `smtp_port` INT NOT NULL DEFAULT 465 COMMENT 'SMTP端口',
    `smtp_username` VARCHAR(100) NOT NULL COMMENT 'SMTP用户名',
    `smtp_password` VARCHAR(255) NOT NULL COMMENT 'SMTP密码(加密存储)',
    `sender_name` VARCHAR(100) DEFAULT NULL COMMENT '发件人显示名称',
    `sender_email` VARCHAR(100) NOT NULL COMMENT '发件人邮箱',
    `use_ssl` TINYINT NOT NULL DEFAULT 1 COMMENT '是否使用SSL: 0=否, 1=是',
    `use_tls` TINYINT NOT NULL DEFAULT 0 COMMENT '是否使用TLS: 0=否, 1=是',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认配置: 0=否, 1=是',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=正常',
    `test_status` TINYINT DEFAULT NULL COMMENT '测试状态: 0=失败, 1=成功',
    `test_time` DATETIME DEFAULT NULL COMMENT '最后测试时间',
    `daily_limit` INT DEFAULT 500 COMMENT '每日发送限制',
    `hourly_limit` INT DEFAULT 100 COMMENT '每小时发送限制',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0=否, 1=是',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB COMMENT='用户SMTP配置表';

-- =====================================================
-- 2. 客户管理相关表
-- =====================================================

-- 客户表
CREATE TABLE IF NOT EXISTS `t_customer` (
    `id` BIGINT NOT NULL COMMENT '客户ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '客户姓名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone` VARCHAR(50) DEFAULT NULL COMMENT '手机号',
    `company` VARCHAR(200) DEFAULT NULL COMMENT '所属公司',
    `position` VARCHAR(100) DEFAULT NULL COMMENT '职位',
    `country` VARCHAR(100) DEFAULT NULL COMMENT '国家',
    `country_code` VARCHAR(10) DEFAULT NULL COMMENT '国家代码(ISO)',
    `address` VARCHAR(500) DEFAULT NULL COMMENT '地址',
    `website` VARCHAR(200) DEFAULT NULL COMMENT '公司官网',
    `priority` TINYINT NOT NULL DEFAULT 3 COMMENT '优先级: 1=最高, 5=最低',
    `meeting_time` DATETIME DEFAULT NULL COMMENT '会面时间',
    `meeting_location` VARCHAR(200) DEFAULT NULL COMMENT '会面地点',
    `wechat_name` VARCHAR(100) DEFAULT NULL COMMENT '微信名称/ID',
    `wechat_qrcode` VARCHAR(500) DEFAULT NULL COMMENT '微信二维码图片URL',
    `whatsapp_name` VARCHAR(100) DEFAULT NULL COMMENT 'WhatsApp名称/号码',
    `whatsapp_qrcode` VARCHAR(500) DEFAULT NULL COMMENT 'WhatsApp二维码图片URL',
    `remark` TEXT COMMENT '备注信息',
    `source` VARCHAR(50) DEFAULT 'manual' COMMENT '来源: manual=手动录入, ocr=名片识别, import=Excel导入',
    `source_file` VARCHAR(500) DEFAULT NULL COMMENT '来源文件(名片图片/Excel文件)',
    `ocr_confidence` DECIMAL(5,2) DEFAULT NULL COMMENT 'OCR识别置信度(百分比)',
    `email_status` TINYINT DEFAULT 1 COMMENT '邮箱状态: 0=无效, 1=有效, 2=退信',
    `last_email_time` DATETIME DEFAULT NULL COMMENT '最后发送邮件时间',
    `email_count` INT DEFAULT 0 COMMENT '已发送邮件数量',
    `extra_data` JSON DEFAULT NULL COMMENT '扩展数据(JSON格式)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0=否, 1=是',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_email` (`email`),
    KEY `idx_priority` (`priority`),
    KEY `idx_country` (`country`),
    KEY `idx_company` (`company`),
    KEY `idx_meeting_time` (`meeting_time`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_source` (`source`)
) ENGINE=InnoDB COMMENT='客户表';

-- 名片识别记录表
CREATE TABLE IF NOT EXISTS `t_business_card` (
    `id` BIGINT NOT NULL COMMENT '名片ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `customer_id` BIGINT DEFAULT NULL COMMENT '关联客户ID(识别成功后)',
    `image_url` VARCHAR(500) NOT NULL COMMENT '名片图片URL',
    `image_thumbnail` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
    `priority` TINYINT NOT NULL DEFAULT 3 COMMENT '优先级: 1=最高, 5=最低',
    `ocr_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'OCR状态: 0=待识别, 1=识别中, 2=识别成功, 3=识别失败',
    `ocr_result` JSON DEFAULT NULL COMMENT 'OCR原始识别结果',
    `ocr_confidence` DECIMAL(5,2) DEFAULT NULL COMMENT 'OCR置信度',
    `ocr_provider` VARCHAR(50) DEFAULT NULL COMMENT 'OCR服务商',
    `ocr_time` DATETIME DEFAULT NULL COMMENT 'OCR识别时间',
    `ocr_error` VARCHAR(500) DEFAULT NULL COMMENT 'OCR错误信息',
    `parsed_name` VARCHAR(100) DEFAULT NULL COMMENT '解析-姓名',
    `parsed_email` VARCHAR(100) DEFAULT NULL COMMENT '解析-邮箱',
    `parsed_phone` VARCHAR(50) DEFAULT NULL COMMENT '解析-电话',
    `parsed_company` VARCHAR(200) DEFAULT NULL COMMENT '解析-公司',
    `parsed_position` VARCHAR(100) DEFAULT NULL COMMENT '解析-职位',
    `parsed_address` VARCHAR(500) DEFAULT NULL COMMENT '解析-地址',
    `parsed_website` VARCHAR(200) DEFAULT NULL COMMENT '解析-网站',
    `has_wechat_qr` TINYINT DEFAULT 0 COMMENT '是否包含微信二维码',
    `has_whatsapp_qr` TINYINT DEFAULT 0 COMMENT '是否包含WhatsApp二维码',
    `is_processed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已处理(转为客户): 0=否, 1=是',
    `upload_batch` VARCHAR(50) DEFAULT NULL COMMENT '上传批次号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0=否, 1=是',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_ocr_status` (`ocr_status`),
    KEY `idx_upload_batch` (`upload_batch`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB COMMENT='名片识别记录表';

-- 自定义字段定义表
CREATE TABLE IF NOT EXISTS `t_custom_field_definition` (
    `id` BIGINT NOT NULL COMMENT '字段ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `field_key` VARCHAR(50) NOT NULL COMMENT '字段键名(英文)',
    `field_name` VARCHAR(100) NOT NULL COMMENT '字段显示名称',
    `field_type` VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT '字段类型: text, number, date, select',
    `field_options` JSON DEFAULT NULL COMMENT '选项值(select类型时使用)',
    `is_required` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填: 0=否, 1=是',
    `default_value` VARCHAR(500) DEFAULT NULL COMMENT '默认值',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0=否, 1=是',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    UNIQUE KEY `uk_user_field` (`user_id`, `field_key`)
) ENGINE=InnoDB COMMENT='自定义字段定义表';

-- 客户导入记录表
CREATE TABLE IF NOT EXISTS `t_customer_import` (
    `id` BIGINT NOT NULL COMMENT '导入ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `file_name` VARCHAR(200) NOT NULL COMMENT '文件名',
    `file_url` VARCHAR(500) NOT NULL COMMENT '文件URL',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
    `total_rows` INT DEFAULT 0 COMMENT '总行数',
    `success_count` INT DEFAULT 0 COMMENT '成功数量',
    `failed_count` INT DEFAULT 0 COMMENT '失败数量',
    `skipped_count` INT DEFAULT 0 COMMENT '跳过数量(重复)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=待处理, 1=处理中, 2=已完成, 3=失败',
    `import_mode` VARCHAR(20) DEFAULT 'append' COMMENT '导入模式: append=追加, overwrite=覆盖, merge=合并',
    `field_mapping` JSON DEFAULT NULL COMMENT '字段映射关系',
    `error_file_url` VARCHAR(500) DEFAULT NULL COMMENT '错误数据文件URL',
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
) ENGINE=InnoDB COMMENT='客户导入记录表';

-- =====================================================
-- 3. 邮件营销相关表
-- =====================================================

-- 邮件模板表
CREATE TABLE IF NOT EXISTS `t_email_template` (
    `id` BIGINT NOT NULL COMMENT '模板ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `subject` VARCHAR(500) NOT NULL COMMENT '邮件主题(支持变量)',
    `content` MEDIUMTEXT NOT NULL COMMENT '邮件正文(HTML格式,支持变量)',
    `content_text` TEXT COMMENT '纯文本版本',
    `variables` JSON DEFAULT NULL COMMENT '使用的变量列表',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '模板分类',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认模板: 0=否, 1=是',
    `use_count` INT DEFAULT 0 COMMENT '使用次数',
    `last_used_at` DATETIME DEFAULT NULL COMMENT '最后使用时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0=否, 1=是',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category` (`category`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB COMMENT='邮件模板表';

-- 邮件发送任务表
CREATE TABLE IF NOT EXISTS `t_email_task` (
    `id` BIGINT NOT NULL COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `task_name` VARCHAR(200) DEFAULT NULL COMMENT '任务名称',
    `template_id` BIGINT NOT NULL COMMENT '使用的模板ID',
    `smtp_config_id` BIGINT NOT NULL COMMENT 'SMTP配置ID',
    `subject` VARCHAR(500) NOT NULL COMMENT '邮件主题(已处理变量)',
    `content` MEDIUMTEXT NOT NULL COMMENT '邮件正文模板',
    `filter_conditions` JSON DEFAULT NULL COMMENT '客户筛选条件',
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '总发送数量',
    `sent_count` INT NOT NULL DEFAULT 0 COMMENT '已发送数量',
    `success_count` INT NOT NULL DEFAULT 0 COMMENT '发送成功数量',
    `failed_count` INT NOT NULL DEFAULT 0 COMMENT '发送失败数量',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=待发送, 1=发送中, 2=已暂停, 3=已完成, 4=已取消',
    `scheduled_at` DATETIME DEFAULT NULL COMMENT '计划发送时间(定时发送)',
    `started_at` DATETIME DEFAULT NULL COMMENT '开始发送时间',
    `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
    `error_message` TEXT COMMENT '错误信息',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0=否, 1=是',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB COMMENT='邮件发送任务表';

-- 邮件发送日志表
CREATE TABLE IF NOT EXISTS `t_email_log` (
    `id` BIGINT NOT NULL COMMENT '日志ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `customer_name` VARCHAR(100) DEFAULT NULL COMMENT '客户姓名(冗余)',
    `customer_email` VARCHAR(100) NOT NULL COMMENT '收件邮箱',
    `customer_company` VARCHAR(200) DEFAULT NULL COMMENT '客户公司(冗余)',
    `customer_country` VARCHAR(100) DEFAULT NULL COMMENT '客户国家(冗余)',
    `customer_priority` TINYINT DEFAULT NULL COMMENT '客户优先级(冗余)',
    `subject` VARCHAR(500) NOT NULL COMMENT '邮件主题(已替换变量)',
    `content` MEDIUMTEXT COMMENT '邮件正文(已替换变量)',
    `variables_data` JSON DEFAULT NULL COMMENT '变量替换数据',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=待发送, 1=发送中, 2=发送成功, 3=发送失败',
    `error_code` VARCHAR(50) DEFAULT NULL COMMENT '错误代码',
    `error_message` TEXT COMMENT '错误信息',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `sent_at` DATETIME DEFAULT NULL COMMENT '发送时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0=否, 1=是',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_customer_email` (`customer_email`),
    KEY `idx_status` (`status`),
    KEY `idx_sent_at` (`sent_at`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB COMMENT='邮件发送日志表';

-- 邮件附件表
CREATE TABLE IF NOT EXISTS `t_email_attachment` (
    `id` BIGINT NOT NULL COMMENT '附件ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `template_id` BIGINT DEFAULT NULL COMMENT '关联模板ID(可为空)',
    `file_name` VARCHAR(200) NOT NULL COMMENT '原始文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
    `file_url` VARCHAR(500) NOT NULL COMMENT '访问URL',
    `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
    `file_type` VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型',
    `file_extension` VARCHAR(20) DEFAULT NULL COMMENT '文件扩展名',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0=否, 1=是',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB COMMENT='邮件附件表';

-- =====================================================
-- 4. 系统功能相关表
-- =====================================================

-- 功能路线图表
CREATE TABLE IF NOT EXISTS `t_roadmap` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `module_name` VARCHAR(100) NOT NULL COMMENT '模块名称',
    `module_key` VARCHAR(50) NOT NULL COMMENT '模块标识',
    `description` TEXT COMMENT '功能描述',
    `features` JSON DEFAULT NULL COMMENT '包含的功能列表',
    `planned_quarter` VARCHAR(20) DEFAULT NULL COMMENT '计划时间(如: 2026 Q1)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'planned' COMMENT '状态: released=已发布, developing=开发中, planned=计划中',
    `interest_count` INT DEFAULT 0 COMMENT '感兴趣人数',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_module_key` (`module_key`)
) ENGINE=InnoDB COMMENT='功能路线图表';

-- =====================================================
-- 5. 初始化数据
-- =====================================================

-- 初始化路线图数据
INSERT INTO `t_roadmap` (`id`, `module_name`, `module_key`, `description`, `planned_quarter`, `status`, `sort_order`) VALUES
(1, '客户管理', 'customer', '名片识别、客户信息管理、Excel导入导出', '2025 Q4', 'released', 1),
(2, '邮件营销', 'email', '邮件模板、批量发送、发送日志追踪', '2025 Q4', 'released', 2),
(3, '邮件追踪增强', 'email-tracking', '邮件打开率、点击率追踪', '2026 Q1', 'developing', 3),
(4, '客户标签系统', 'customer-tag', '自定义标签、智能标签', '2026 Q1', 'planned', 4),
(5, '移动端H5适配', 'mobile-h5', '手机浏览器访问适配', '2026 Q1', 'planned', 5),
(6, '销售管理', 'sales', '报价单、合同、订单管理', '2026 Q2', 'planned', 6),
(7, '物流跟踪', 'logistics', '货运追踪、清关进度', '2026 Q3', 'planned', 7),
(8, '供应链管理', 'supply-chain', '供应商、采购、库存管理', '2026 Q3', 'planned', 8),
(9, '文档中心', 'document', '合同、发票、证书管理', '2026 Q4', 'planned', 9),
(10, '数据分析', 'analytics', '销售报表、客户分析、业务预测', '2027', 'planned', 10)
ON DUPLICATE KEY UPDATE `module_name` = VALUES(`module_name`);

