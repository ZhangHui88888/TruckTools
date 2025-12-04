# TruckTools 技术架构文档

## 卡车外贸综合工具平台 - MVP版本

**版本**: v1.0  
**创建日期**: 2025年12月2日  
**技术负责人**: [待定]  
**最后更新**: 2025年12月2日

---

## 目录

1. [技术架构概述](#1-技术架构概述)
2. [技术栈选型](#2-技术栈选型)
3. [系统架构设计](#3-系统架构设计)
4. [数据库设计](#4-数据库设计)
5. [API接口设计](#5-api接口设计)
6. [安全设计](#6-安全设计)
7. [部署架构](#7-部署架构)
8. [开发规范](#8-开发规范)

---

## 1. 技术架构概述

### 1.1 架构设计原则

| 原则 | 说明 |
|------|------|
| **模块化设计** | 各业务模块独立，便于后续扩展和维护 |
| **前后端分离** | 前端Vue3 + 后端Spring Boot，通过RESTful API通信 |
| **高可用性** | 核心服务无单点故障，支持水平扩展 |
| **安全优先** | 全链路加密，严格的权限控制 |
| **可观测性** | 完善的日志、监控、告警体系 |

### 1.2 系统边界

```
┌─────────────────────────────────────────────────────────────────┐
│                        用户层 (User Layer)                        │
│                   浏览器 (Chrome/Edge/Firefox)                    │
└─────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                      网关层 (Gateway Layer)                       │
│                    Nginx / Spring Cloud Gateway                  │
└─────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                      应用层 (Application Layer)                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐   │
│  │ 用户服务 │ │ 客户服务 │ │ 邮件服务 │ │ OCR识别服务       │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                      数据层 (Data Layer)                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐   │
│  │  MySQL   │ │  Redis   │ │   OSS    │ │ Elasticsearch    │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 技术栈选型

### 2.1 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue.js | 3.4+ | 前端框架 |
| TypeScript | 5.0+ | 类型安全 |
| Vite | 5.0+ | 构建工具 |
| Ant Design Vue | 4.0+ | UI组件库 |
| Pinia | 2.1+ | 状态管理 |
| Vue Router | 4.2+ | 路由管理 |
| Axios | 1.6+ | HTTP请求 |
| TinyMCE/Quill | - | 富文本编辑器 |
| ECharts | 5.4+ | 图表组件 |
| xlsx | 0.18+ | Excel处理 |

### 2.2 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 LTS | 开发语言 |
| Spring Boot | 3.2+ | 应用框架 |
| Spring Security | 6.2+ | 安全框架 |
| MyBatis-Plus | 3.5+ | ORM框架 |
| Spring Mail | - | 邮件发送 |
| JWT | - | 身份认证 |
| Hutool | 5.8+ | 工具库 |
| EasyExcel | 3.3+ | Excel处理 |
| Lombok | 1.18+ | 代码简化 |
| MapStruct | 1.5+ | 对象映射 |
| Validation | - | 参数校验 |

### 2.3 中间件与基础设施

| 技术 | 版本 | 用途 |
|------|------|------|
| MySQL | 8.0+ | 主数据库 |
| Redis | 7.0+ | 缓存/会话 |
| MinIO/OSS | - | 对象存储 |
| Nginx | 1.24+ | 反向代理/负载均衡 |
| RabbitMQ | 3.12+ | 消息队列 |
| Elasticsearch | 8.0+ | 全文搜索(可选) |

### 2.4 第三方服务

| 服务 | 提供商 | 用途 |
|------|------|------|
| OCR服务 | 百度/腾讯/阿里云 | 名片识别 |
| 邮件服务 | 用户自有SMTP | 邮件发送 |
| 短信服务 | 阿里云/腾讯云 | 验证码(可选) |

---

## 3. 系统架构设计

### 3.1 后端模块划分

```
truck-tools/
├── truck-tools-common/          # 公共模块
│   ├── common-core/             # 核心工具类、常量、异常
│   ├── common-redis/            # Redis配置与工具
│   ├── common-security/         # 安全认证
│   └── common-mybatis/          # MyBatis配置
├── truck-tools-api/             # API接口定义
│   ├── api-system/              # 系统服务API
│   ├── api-customer/            # 客户服务API
│   └── api-email/               # 邮件服务API
├── truck-tools-modules/         # 业务模块
│   ├── module-system/           # 系统管理模块
│   ├── module-customer/         # 客户管理模块
│   ├── module-email/            # 邮件营销模块
│   └── module-ocr/              # OCR识别模块
├── truck-tools-gateway/         # 网关服务(可选)
└── truck-tools-admin/           # 后台管理服务入口
```

### 3.2 前端目录结构

```
truck-tools-web/
├── public/
├── src/
│   ├── api/                     # API接口定义
│   │   ├── auth.ts
│   │   ├── customer.ts
│   │   ├── email.ts
│   │   └── system.ts
│   ├── assets/                  # 静态资源
│   ├── components/              # 公共组件
│   │   ├── BusinessCard/        # 名片组件
│   │   ├── EmailEditor/         # 邮件编辑器
│   │   └── common/              # 通用组件
│   ├── layouts/                 # 布局组件
│   │   ├── MainLayout.vue
│   │   └── SideMenu.vue
│   ├── router/                  # 路由配置
│   │   ├── index.ts
│   │   └── modules/
│   ├── stores/                  # Pinia状态管理
│   ├── utils/                   # 工具函数
│   ├── views/                   # 页面视图
│   │   ├── auth/                # 登录注册
│   │   ├── customer/            # 客户管理
│   │   ├── email/               # 邮件营销
│   │   ├── dashboard/           # 工作台
│   │   ├── roadmap/             # 功能路线图
│   │   └── settings/            # 系统设置
│   ├── App.vue
│   └── main.ts
├── .env.development
├── .env.production
├── package.json
├── tsconfig.json
└── vite.config.ts
```

### 3.3 核心业务流程

#### 3.3.1 名片识别流程

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│ 上传图片 │───▶│ 存储OSS │───▶│ 调用OCR │───▶│ 解析结果 │───▶│ 保存客户 │
└─────────┘    └─────────┘    └─────────┘    └─────────┘    └─────────┘
     │                                             │
     ▼                                             ▼
┌─────────┐                                  ┌─────────┐
│ 设置优先级│                                  │ 人工校正 │
└─────────┘                                  └─────────┘
```

#### 3.3.2 邮件发送流程

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│ 选择模板 │───▶│ 筛选客户 │───▶│ 预览邮件 │───▶│ 提交任务 │───▶│ MQ队列  │
└─────────┘    └─────────┘    └─────────┘    └─────────┘    └─────────┘
                                                                  │
                                                                  ▼
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│ 查看日志 │◀───│ 更新状态 │◀───│ 记录结果 │◀───│ SMTP发送 │◀───│ 消费消息 │
└─────────┘    └─────────┘    └─────────┘    └─────────┘    └─────────┘
```

---

## 4. 数据库设计

### 4.1 数据库规范

- **命名规范**: 表名使用小写+下划线，如 `t_customer`
- **字符集**: `utf8mb4`，排序规则 `utf8mb4_general_ci`
- **主键**: 使用雪花算法生成的BIGINT类型ID
- **时间字段**: 统一使用 `DATETIME` 类型，存储UTC时间
- **软删除**: 使用 `deleted` 字段标记，0=正常，1=已删除
- **扩展字段**: 预留 `extra_data` JSON字段用于扩展

### 4.2 ER图概览

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   t_user    │       │ t_customer  │       │t_email_template│
│─────────────│       │─────────────│       │─────────────│
│ id          │──┐    │ id          │    ┌──│ id          │
│ username    │  │    │ user_id     │◀───┤  │ user_id     │
│ email       │  │    │ name        │    │  │ name        │
│ ...         │  │    │ email       │    │  │ subject     │
└─────────────┘  │    │ priority    │    │  │ content     │
                 │    │ ...         │    │  │ ...         │
                 │    └─────────────┘    │  └─────────────┘
                 │           │           │         │
                 │           ▼           │         ▼
                 │    ┌─────────────┐    │  ┌─────────────┐
                 │    │t_custom_field│    │  │t_email_task │
                 │    │─────────────│    │  │─────────────│
                 └───▶│ user_id     │    │  │ id          │
                      │ field_name  │    └─▶│ template_id │
                      │ field_value │       │ status      │
                      │ ...         │       │ ...         │
                      └─────────────┘       └─────────────┘
                                                   │
                                                   ▼
                                            ┌─────────────┐
                                            │t_email_log  │
                                            │─────────────│
                                            │ id          │
                                            │ task_id     │
                                            │ customer_id │
                                            │ status      │
                                            │ ...         │
                                            └─────────────┘
```

### 4.3 SQL建表语句

```sql
-- =====================================================
-- TruckTools 数据库建表脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `truck_tools` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;

USE `truck_tools`;

-- =====================================================
-- 1. 用户与认证相关表
-- =====================================================

-- 用户表
CREATE TABLE `t_user` (
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
CREATE TABLE `t_user_smtp_config` (
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
CREATE TABLE `t_customer` (
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
    `wechat_qrcode` VARCHAR(500) DEFAULT NULL COMMENT '微信二维码图片URL',
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

-- 自定义字段定义表
CREATE TABLE `t_custom_field_definition` (
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

-- 客户自定义字段值表
CREATE TABLE `t_customer_custom_field` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `field_id` BIGINT NOT NULL COMMENT '字段定义ID',
    `field_value` TEXT COMMENT '字段值',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_field_id` (`field_id`),
    UNIQUE KEY `uk_customer_field` (`customer_id`, `field_id`)
) ENGINE=InnoDB COMMENT='客户自定义字段值表';

-- 名片识别记录表
CREATE TABLE `t_business_card` (
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

-- 客户导入记录表
CREATE TABLE `t_customer_import` (
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
CREATE TABLE `t_email_template` (
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
CREATE TABLE `t_email_task` (
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
CREATE TABLE `t_email_log` (
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
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_customer_email` (`customer_email`),
    KEY `idx_status` (`status`),
    KEY `idx_sent_at` (`sent_at`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB COMMENT='邮件发送日志表';

-- 邮件黑名单表
CREATE TABLE `t_email_blacklist` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    `reason` VARCHAR(200) DEFAULT NULL COMMENT '加入原因',
    `source` VARCHAR(50) DEFAULT 'manual' COMMENT '来源: manual=手动添加, bounce=退信, unsubscribe=退订',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    UNIQUE KEY `uk_user_email` (`user_id`, `email`)
) ENGINE=InnoDB COMMENT='邮件黑名单表';

-- =====================================================
-- 4. 系统功能相关表
-- =====================================================

-- 功能路线图表
CREATE TABLE `t_roadmap` (
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

-- 用户订阅通知表
CREATE TABLE `t_user_subscription` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `roadmap_id` BIGINT NOT NULL COMMENT '路线图模块ID',
    `notify_email` TINYINT DEFAULT 1 COMMENT '是否邮件通知: 0=否, 1=是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_roadmap` (`user_id`, `roadmap_id`)
) ENGINE=InnoDB COMMENT='用户订阅通知表';

-- 操作日志表
CREATE TABLE `t_operation_log` (
    `id` BIGINT NOT NULL COMMENT '日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `module` VARCHAR(50) NOT NULL COMMENT '模块',
    `operation` VARCHAR(100) NOT NULL COMMENT '操作类型',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    `request_method` VARCHAR(10) DEFAULT NULL COMMENT 'HTTP方法',
    `request_params` TEXT COMMENT '请求参数',
    `response_data` TEXT COMMENT '响应数据',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'User Agent',
    `duration` BIGINT DEFAULT NULL COMMENT '执行时长(毫秒)',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0=失败, 1=成功',
    `error_message` TEXT COMMENT '错误信息',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB COMMENT='操作日志表';

-- 系统配置表
CREATE TABLE `t_system_config` (
    `id` BIGINT NOT NULL COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(20) DEFAULT 'string' COMMENT '值类型: string, number, boolean, json',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '配置说明',
    `is_public` TINYINT DEFAULT 0 COMMENT '是否公开(前端可见): 0=否, 1=是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB COMMENT='系统配置表';

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
(10, '数据分析', 'analytics', '销售报表、客户分析、业务预测', '2027', 'planned', 10);

-- 初始化系统配置
INSERT INTO `t_system_config` (`id`, `config_key`, `config_value`, `config_type`, `description`, `is_public`) VALUES
(1, 'system.name', 'TruckTools', 'string', '系统名称', 1),
(2, 'system.version', '1.0.0', 'string', '系统版本', 1),
(3, 'email.daily_limit', '500', 'number', '每日邮件发送限制', 0),
(4, 'email.hourly_limit', '100', 'number', '每小时邮件发送限制', 0),
(5, 'upload.max_file_size', '10485760', 'number', '最大上传文件大小(字节)', 0),
(6, 'customer.import_max_rows', '10000', 'number', 'Excel导入最大行数', 0),
(7, 'ocr.provider', 'baidu', 'string', 'OCR服务商', 0);
```

### 4.4 索引设计说明

| 表名 | 索引 | 类型 | 说明 |
|------|------|------|------|
| t_customer | idx_user_id | 普通 | 用户查询自己的客户 |
| t_customer | idx_priority | 普通 | 按优先级排序 |
| t_customer | idx_email | 普通 | 邮箱查重 |
| t_email_log | idx_task_id | 普通 | 任务下的日志查询 |
| t_email_log | idx_status | 普通 | 按状态筛选 |
| t_email_log | idx_sent_at | 普通 | 按发送时间查询 |

---

## 5. API接口设计

### 5.1 API设计规范

#### 5.1.1 基础规范

- **基础路径**: `/api/v1`
- **请求格式**: `application/json`
- **响应格式**: `application/json`
- **认证方式**: Bearer Token (JWT)
- **时间格式**: ISO 8601 (`2025-12-02T10:30:00Z`)

#### 5.1.2 统一响应格式

```json
{
    "code": 200,
    "message": "success",
    "data": {},
    "timestamp": 1733126400000
}
```

**状态码定义**:

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

#### 5.1.3 分页请求参数

```json
{
    "page": 1,
    "pageSize": 20,
    "sortField": "createdAt",
    "sortOrder": "desc"
}
```

#### 5.1.4 分页响应格式

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "list": [],
        "pagination": {
            "page": 1,
            "pageSize": 20,
            "total": 100,
            "totalPages": 5
        }
    }
}
```

---

### 5.2 认证模块 API

#### 5.2.1 用户注册

```
POST /api/v1/auth/register
```

**请求参数**:

```json
{
    "username": "zhangsan",
    "email": "zhangsan@example.com",
    "password": "Password123!",
    "confirmPassword": "Password123!"
}
```

**响应**:

```json
{
    "code": 200,
    "message": "注册成功，请查收验证邮件",
    "data": {
        "userId": "1234567890123456789"
    }
}
```

#### 5.2.2 用户登录

```
POST /api/v1/auth/login
```

**请求参数**:

```json
{
    "username": "zhangsan",
    "password": "Password123!"
}
```

**响应**:

```json
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
        "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
        "expiresIn": 604800,
        "user": {
            "id": "1234567890123456789",
            "username": "zhangsan",
            "email": "zhangsan@example.com",
            "nickname": "张三",
            "avatar": "https://oss.example.com/avatar/xxx.jpg"
        }
    }
}
```

#### 5.2.3 刷新Token

```
POST /api/v1/auth/refresh
```

**请求参数**:

```json
{
    "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

#### 5.2.4 退出登录

```
POST /api/v1/auth/logout
```

#### 5.2.5 发送密码重置邮件

```
POST /api/v1/auth/forgot-password
```

**请求参数**:

```json
{
    "email": "zhangsan@example.com"
}
```

#### 5.2.6 重置密码

```
POST /api/v1/auth/reset-password
```

**请求参数**:

```json
{
    "token": "reset_token_xxx",
    "newPassword": "NewPassword123!",
    "confirmPassword": "NewPassword123!"
}
```

---

### 5.3 客户管理 API

#### 5.3.1 获取客户列表

```
GET /api/v1/customers
```

**请求参数** (Query):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认20，最大100 |
| keyword | string | 否 | 关键词搜索(姓名/公司/邮箱) |
| priority | int | 否 | 优先级筛选(1-5) |
| country | string | 否 | 国家筛选 |
| source | string | 否 | 来源筛选(manual/ocr/import) |
| startDate | string | 否 | 会面开始时间 |
| endDate | string | 否 | 会面结束时间 |
| sortField | string | 否 | 排序字段 |
| sortOrder | string | 否 | 排序方向(asc/desc) |

**响应**:

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "list": [
            {
                "id": "1234567890123456789",
                "name": "John Smith",
                "email": "john@company.com",
                "phone": "+1-123-456-7890",
                "company": "ABC Trading Co.",
                "position": "Sales Manager",
                "country": "美国",
                "countryCode": "US",
                "website": "https://www.abc-trading.com",
                "priority": 1,
                "meetingTime": "2025-11-28T09:00:00Z",
                "meetingLocation": "广交会A馆",
                "remark": "对重型卡车配件感兴趣",
                "source": "ocr",
                "emailStatus": 1,
                "lastEmailTime": "2025-11-30T10:00:00Z",
                "emailCount": 2,
                "customFields": {
                    "productInterest": "卡车轮胎",
                    "budget": "100000"
                },
                "createdAt": "2025-11-28T10:30:00Z",
                "updatedAt": "2025-12-01T14:20:00Z"
            }
        ],
        "pagination": {
            "page": 1,
            "pageSize": 20,
            "total": 156,
            "totalPages": 8
        }
    }
}
```

#### 5.3.2 获取客户详情

```
GET /api/v1/customers/{id}
```

**响应**: 同上单个客户对象，额外包含:

```json
{
    "wechatQrcode": "https://oss.example.com/qrcode/wechat_xxx.jpg",
    "whatsappQrcode": "https://oss.example.com/qrcode/whatsapp_xxx.jpg",
    "sourceFile": "https://oss.example.com/cards/xxx.jpg",
    "ocrConfidence": 92.5
}
```

#### 5.3.3 创建客户

```
POST /api/v1/customers
```

**请求参数**:

```json
{
    "name": "John Smith",
    "email": "john@company.com",
    "phone": "+1-123-456-7890",
    "company": "ABC Trading Co.",
    "position": "Sales Manager",
    "country": "美国",
    "website": "https://www.abc-trading.com",
    "address": "123 Main St, New York",
    "priority": 2,
    "meetingTime": "2025-11-28T09:00:00Z",
    "meetingLocation": "广交会A馆",
    "remark": "对重型卡车配件感兴趣",
    "customFields": {
        "productInterest": "卡车轮胎"
    }
}
```

#### 5.3.4 更新客户

```
PUT /api/v1/customers/{id}
```

**请求参数**: 同创建客户

#### 5.3.5 删除客户

```
DELETE /api/v1/customers/{id}
```

#### 5.3.6 批量删除客户

```
DELETE /api/v1/customers/batch
```

**请求参数**:

```json
{
    "ids": ["id1", "id2", "id3"]
}
```

#### 5.3.7 导出客户

```
POST /api/v1/customers/export
```

**请求参数**:

```json
{
    "ids": [],
    "filter": {
        "priority": 1,
        "country": "美国"
    },
    "fields": ["name", "email", "company", "phone", "country", "priority"],
    "format": "xlsx"
}
```

**响应**:

```json
{
    "code": 200,
    "data": {
        "downloadUrl": "https://oss.example.com/exports/customers_20251202.xlsx",
        "expiresAt": "2025-12-03T10:00:00Z"
    }
}
```

---

### 5.4 名片识别 API

#### 5.4.1 上传名片

```
POST /api/v1/business-cards/upload
Content-Type: multipart/form-data
```

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| files | File[] | 是 | 名片图片(最多50张) |
| priorities | string | 否 | 优先级数组JSON，如"[1,2,3]" |

**响应**:

```json
{
    "code": 200,
    "message": "上传成功，正在识别",
    "data": {
        "batchId": "batch_20251202_001",
        "totalCount": 5,
        "cards": [
            {
                "id": "card_001",
                "imageUrl": "https://oss.example.com/cards/xxx.jpg",
                "priority": 1,
                "ocrStatus": 0
            }
        ]
    }
}
```

#### 5.4.2 获取识别结果

```
GET /api/v1/business-cards/{id}
```

**响应**:

```json
{
    "code": 200,
    "data": {
        "id": "card_001",
        "imageUrl": "https://oss.example.com/cards/xxx.jpg",
        "thumbnailUrl": "https://oss.example.com/cards/xxx_thumb.jpg",
        "priority": 1,
        "ocrStatus": 2,
        "ocrConfidence": 92.5,
        "parsedData": {
            "name": "John Smith",
            "email": "john@company.com",
            "phone": "+1-123-456-7890",
            "company": "ABC Trading Co.",
            "position": "Sales Manager",
            "address": "123 Main St, New York",
            "website": "www.abc-trading.com"
        },
        "hasWechatQr": true,
        "hasWhatsappQr": false,
        "isProcessed": false,
        "createdAt": "2025-12-02T10:30:00Z"
    }
}
```

#### 5.4.3 查询批次识别状态

```
GET /api/v1/business-cards/batch/{batchId}/status
```

**响应**:

```json
{
    "code": 200,
    "data": {
        "batchId": "batch_20251202_001",
        "totalCount": 5,
        "pendingCount": 0,
        "processingCount": 1,
        "successCount": 3,
        "failedCount": 1,
        "cards": [
            {
                "id": "card_001",
                "ocrStatus": 2,
                "ocrConfidence": 92.5
            }
        ]
    }
}
```

#### 5.4.4 确认识别结果转为客户

```
POST /api/v1/business-cards/{id}/confirm
```

**请求参数**:

```json
{
    "name": "John Smith",
    "email": "john@company.com",
    "phone": "+1-123-456-7890",
    "company": "ABC Trading Co.",
    "position": "Sales Manager",
    "country": "美国",
    "website": "https://www.abc-trading.com",
    "priority": 1,
    "meetingTime": "2025-11-28T09:00:00Z",
    "meetingLocation": "广交会A馆",
    "remark": "手动修正后确认"
}
```

**响应**:

```json
{
    "code": 200,
    "message": "客户创建成功",
    "data": {
        "customerId": "cust_123456789"
    }
}
```

#### 5.4.5 批量确认转为客户

```
POST /api/v1/business-cards/batch-confirm
```

**请求参数**:

```json
{
    "cardIds": ["card_001", "card_002", "card_003"]
}
```

---

### 5.5 Excel导入 API

#### 5.5.1 下载导入模板

```
GET /api/v1/customers/import/template
```

**响应**: 直接返回Excel文件流

#### 5.5.2 上传Excel文件

```
POST /api/v1/customers/import/upload
Content-Type: multipart/form-data
```

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | Excel文件(.xlsx) |

**响应**:

```json
{
    "code": 200,
    "data": {
        "importId": "import_20251202_001",
        "fileName": "customers.xlsx",
        "totalRows": 1250,
        "headers": ["姓名", "邮箱", "公司", "电话", "国家"],
        "suggestedMapping": {
            "姓名": "name",
            "邮箱": "email",
            "公司": "company",
            "电话": "phone",
            "国家": "country"
        },
        "previewData": [
            {"姓名": "John", "邮箱": "john@test.com", "公司": "ABC"},
            {"姓名": "Jane", "邮箱": "jane@test.com", "公司": "XYZ"}
        ]
    }
}
```

#### 5.5.3 确认字段映射并预检

```
POST /api/v1/customers/import/{importId}/validate
```

**请求参数**:

```json
{
    "fieldMapping": {
        "姓名": "name",
        "邮箱": "email",
        "公司": "company",
        "电话": "phone",
        "国家": "country"
    },
    "importMode": "append"
}
```

**响应**:

```json
{
    "code": 200,
    "data": {
        "totalRows": 1250,
        "validRows": 1220,
        "invalidRows": 30,
        "duplicateRows": 15,
        "errors": [
            {"row": 5, "field": "email", "value": "invalid-email", "message": "邮箱格式错误"},
            {"row": 12, "field": "name", "value": "", "message": "姓名不能为空"}
        ],
        "errorFileUrl": "https://oss.example.com/imports/errors_xxx.xlsx"
    }
}
```

#### 5.5.4 执行导入

```
POST /api/v1/customers/import/{importId}/execute
```

**请求参数**:

```json
{
    "importMode": "append",
    "duplicateAction": "skip"
}
```

**响应**:

```json
{
    "code": 200,
    "message": "导入任务已开始",
    "data": {
        "importId": "import_20251202_001",
        "status": "processing"
    }
}
```

#### 5.5.5 查询导入状态

```
GET /api/v1/customers/import/{importId}/status
```

**响应**:

```json
{
    "code": 200,
    "data": {
        "importId": "import_20251202_001",
        "status": "completed",
        "totalRows": 1250,
        "successCount": 1200,
        "failedCount": 30,
        "skippedCount": 20,
        "progress": 100,
        "startedAt": "2025-12-02T10:30:00Z",
        "completedAt": "2025-12-02T10:30:45Z",
        "logFileUrl": "https://oss.example.com/imports/log_xxx.xlsx"
    }
}
```

---

### 5.6 邮件模板 API

#### 5.6.1 获取模板列表

```
GET /api/v1/email/templates
```

**响应**:

```json
{
    "code": 200,
    "data": {
        "list": [
            {
                "id": "tpl_001",
                "name": "展会感谢信",
                "subject": "感谢您在{{meetingLocation}}与我们会面",
                "category": "展会跟进",
                "useCount": 15,
                "lastUsedAt": "2025-12-01T10:00:00Z",
                "createdAt": "2025-11-20T10:00:00Z"
            }
        ],
        "pagination": {...}
    }
}
```

#### 5.6.2 获取模板详情

```
GET /api/v1/email/templates/{id}
```

**响应**:

```json
{
    "code": 200,
    "data": {
        "id": "tpl_001",
        "name": "展会感谢信",
        "subject": "感谢您在{{meetingLocation}}与我们会面",
        "content": "<p>尊敬的{{name}}先生/女士，</p><p>感谢您在{{meetingLocation}}与我们会面...</p>",
        "contentText": "尊敬的{{name}}先生/女士，感谢您在{{meetingLocation}}与我们会面...",
        "variables": ["name", "meetingLocation", "company"],
        "category": "展会跟进",
        "createdAt": "2025-11-20T10:00:00Z"
    }
}
```

#### 5.6.3 创建模板

```
POST /api/v1/email/templates
```

**请求参数**:

```json
{
    "name": "展会感谢信",
    "subject": "感谢您在{{meetingLocation}}与我们会面",
    "content": "<p>尊敬的{{name}}先生/女士，</p><p>感谢您在{{meetingLocation}}与我们会面...</p>",
    "category": "展会跟进"
}
```

#### 5.6.4 更新模板

```
PUT /api/v1/email/templates/{id}
```

#### 5.6.5 删除模板

```
DELETE /api/v1/email/templates/{id}
```

#### 5.6.6 获取可用变量列表

```
GET /api/v1/email/templates/variables
```

**响应**:

```json
{
    "code": 200,
    "data": {
        "systemVariables": [
            {"key": "name", "label": "客户姓名", "example": "John Smith"},
            {"key": "email", "label": "邮箱", "example": "john@example.com"},
            {"key": "company", "label": "公司名称", "example": "ABC Trading"},
            {"key": "position", "label": "职位", "example": "Sales Manager"},
            {"key": "country", "label": "国家", "example": "美国"},
            {"key": "meetingTime", "label": "会面时间", "example": "2025-11-28"},
            {"key": "meetingLocation", "label": "会面地点", "example": "广交会"}
        ],
        "customVariables": [
            {"key": "productInterest", "label": "感兴趣产品", "fieldId": "field_001"}
        ]
    }
}
```

#### 5.6.7 预览邮件

```
POST /api/v1/email/templates/preview
```

**请求参数**:

```json
{
    "subject": "感谢您在{{meetingLocation}}与我们会面",
    "content": "<p>尊敬的{{name}}先生/女士...</p>",
    "customerId": "cust_123456"
}
```

**响应**:

```json
{
    "code": 200,
    "data": {
        "subject": "感谢您在广交会与我们会面",
        "content": "<p>尊敬的John Smith先生/女士...</p>",
        "customer": {
            "name": "John Smith",
            "email": "john@example.com",
            "company": "ABC Trading"
        }
    }
}
```

---

### 5.7 邮件发送 API

#### 5.7.1 创建发送任务

```
POST /api/v1/email/tasks
```

**请求参数**:

```json
{
    "taskName": "展会客户第一轮跟进",
    "templateId": "tpl_001",
    "smtpConfigId": "smtp_001",
    "customerIds": ["cust_001", "cust_002"],
    "filter": {
        "priority": [1, 2],
        "country": ["美国", "德国"]
    },
    "scheduledAt": null
}
```

**响应**:

```json
{
    "code": 200,
    "message": "发送任务已创建",
    "data": {
        "taskId": "task_20251202_001",
        "totalCount": 45,
        "status": 0
    }
}
```

#### 5.7.2 开始发送

```
POST /api/v1/email/tasks/{taskId}/start
```

#### 5.7.3 暂停发送

```
POST /api/v1/email/tasks/{taskId}/pause
```

#### 5.7.4 继续发送

```
POST /api/v1/email/tasks/{taskId}/resume
```

#### 5.7.5 取消任务

```
POST /api/v1/email/tasks/{taskId}/cancel
```

#### 5.7.6 获取任务状态

```
GET /api/v1/email/tasks/{taskId}
```

**响应**:

```json
{
    "code": 200,
    "data": {
        "taskId": "task_20251202_001",
        "taskName": "展会客户第一轮跟进",
        "templateName": "展会感谢信",
        "status": 1,
        "statusText": "发送中",
        "totalCount": 45,
        "sentCount": 30,
        "successCount": 28,
        "failedCount": 2,
        "progress": 66.7,
        "startedAt": "2025-12-02T10:30:00Z",
        "estimatedEndTime": "2025-12-02T10:35:00Z"
    }
}
```

#### 5.7.7 获取任务列表

```
GET /api/v1/email/tasks
```

#### 5.7.8 获取发送日志

```
GET /api/v1/email/tasks/{taskId}/logs
```

**请求参数** (Query):

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| pageSize | int | 每页数量 |
| status | int | 状态筛选(0/1/2/3) |

**响应**:

```json
{
    "code": 200,
    "data": {
        "list": [
            {
                "id": "log_001",
                "customerName": "John Smith",
                "customerEmail": "john@example.com",
                "customerCompany": "ABC Trading",
                "customerCountry": "美国",
                "customerPriority": 1,
                "subject": "感谢您在广交会与我们会面",
                "status": 2,
                "statusText": "发送成功",
                "sentAt": "2025-12-02T10:30:05Z"
            },
            {
                "id": "log_002",
                "customerName": "Jane Doe",
                "customerEmail": "jane@invalid",
                "status": 3,
                "statusText": "发送失败",
                "errorCode": "INVALID_EMAIL",
                "errorMessage": "邮箱地址无效"
            }
        ],
        "pagination": {...}
    }
}
```

#### 5.7.9 重试失败的邮件

```
POST /api/v1/email/tasks/{taskId}/retry
```

**请求参数**:

```json
{
    "logIds": ["log_002", "log_005"]
}
```

#### 5.7.10 导出发送日志

```
POST /api/v1/email/tasks/{taskId}/export
```

**响应**:

```json
{
    "code": 200,
    "data": {
        "downloadUrl": "https://oss.example.com/exports/email_log_xxx.xlsx",
        "expiresAt": "2025-12-03T10:00:00Z"
    }
}
```

---

### 5.8 SMTP配置 API

#### 5.8.1 获取SMTP配置列表

```
GET /api/v1/settings/smtp
```

#### 5.8.2 创建SMTP配置

```
POST /api/v1/settings/smtp
```

**请求参数**:

```json
{
    "configName": "公司邮箱",
    "smtpHost": "smtp.company.com",
    "smtpPort": 465,
    "smtpUsername": "sales@company.com",
    "smtpPassword": "password123",
    "senderName": "ABC公司销售部",
    "senderEmail": "sales@company.com",
    "useSsl": true,
    "useTls": false,
    "isDefault": true
}
```

#### 5.8.3 测试SMTP连接

```
POST /api/v1/settings/smtp/{id}/test
```

**响应**:

```json
{
    "code": 200,
    "data": {
        "success": true,
        "message": "连接成功，测试邮件已发送"
    }
}
```

#### 5.8.4 更新SMTP配置

```
PUT /api/v1/settings/smtp/{id}
```

#### 5.8.5 删除SMTP配置

```
DELETE /api/v1/settings/smtp/{id}
```

---

### 5.9 自定义字段 API

#### 5.9.1 获取自定义字段列表

```
GET /api/v1/custom-fields
```

#### 5.9.2 创建自定义字段

```
POST /api/v1/custom-fields
```

**请求参数**:

```json
{
    "fieldKey": "productInterest",
    "fieldName": "感兴趣产品",
    "fieldType": "text",
    "isRequired": false,
    "defaultValue": ""
}
```

#### 5.9.3 更新自定义字段

```
PUT /api/v1/custom-fields/{id}
```

#### 5.9.4 删除自定义字段

```
DELETE /api/v1/custom-fields/{id}
```

#### 5.9.5 批量更新客户自定义字段值

```
POST /api/v1/custom-fields/batch-update
```

**请求参数**:

```json
{
    "customerIds": ["cust_001", "cust_002"],
    "fieldId": "field_001",
    "fieldValue": "卡车轮胎"
}
```

---

### 5.10 路线图 API

#### 5.10.1 获取路线图列表

```
GET /api/v1/roadmap
```

**响应**:

```json
{
    "code": 200,
    "data": [
        {
            "id": 1,
            "moduleName": "客户管理",
            "moduleKey": "customer",
            "description": "名片识别、客户信息管理、Excel导入导出",
            "features": ["名片OCR识别", "客户表格管理", "Excel批量导入"],
            "plannedQuarter": "2025 Q4",
            "status": "released",
            "interestCount": 0,
            "isSubscribed": false
        },
        {
            "id": 6,
            "moduleName": "销售管理",
            "moduleKey": "sales",
            "description": "报价单、合同、订单管理",
            "features": ["报价单管理", "合同管理", "订单管理", "销售漏斗"],
            "plannedQuarter": "2026 Q2",
            "status": "planned",
            "interestCount": 128,
            "isSubscribed": true
        }
    ]
}
```

#### 5.10.2 订阅模块上线通知

```
POST /api/v1/roadmap/{id}/subscribe
```

#### 5.10.3 取消订阅

```
DELETE /api/v1/roadmap/{id}/subscribe
```

#### 5.10.4 表达兴趣(+1)

```
POST /api/v1/roadmap/{id}/interest
```

---

### 5.11 用户设置 API

#### 5.11.1 获取个人资料

```
GET /api/v1/user/profile
```

#### 5.11.2 更新个人资料

```
PUT /api/v1/user/profile
```

**请求参数**:

```json
{
    "nickname": "张三",
    "phone": "13800138000"
}
```

#### 5.11.3 上传头像

```
POST /api/v1/user/avatar
Content-Type: multipart/form-data
```

#### 5.11.4 修改密码

```
PUT /api/v1/user/password
```

**请求参数**:

```json
{
    "oldPassword": "OldPassword123!",
    "newPassword": "NewPassword123!",
    "confirmPassword": "NewPassword123!"
}
```

---

## 6. 安全设计

### 6.1 认证与授权

#### 6.1.1 JWT Token设计

```java
// Token载荷结构
{
    "sub": "1234567890123456789",  // 用户ID
    "username": "zhangsan",
    "iat": 1733126400,             // 签发时间
    "exp": 1733731200,             // 过期时间(7天)
    "type": "access"               // token类型
}
```

#### 6.1.2 Token刷新机制

- Access Token有效期: 7天
- Refresh Token有效期: 30天
- 支持Token黑名单机制(退出登录后Token失效)

### 6.2 数据安全

#### 6.2.1 敏感数据加密

```java
// 使用AES加密敏感字段
@ColumnEncrypt
private String smtpPassword;

@ColumnEncrypt  
private String phone;
```

#### 6.2.2 密码存储

- 使用BCrypt算法加密存储
- 强度因子: 12

### 6.3 接口安全

| 安全措施 | 实现方式 |
|----------|----------|
| HTTPS | 全站强制HTTPS |
| CSRF | 使用CSRF Token |
| XSS | 输入过滤 + 输出转义 |
| SQL注入 | MyBatis参数化查询 |
| 限流 | 基于Redis的滑动窗口限流 |

### 6.4 限流策略

```java
// 接口限流配置
@RateLimiter(key = "email:send", rate = 100, interval = 3600)  // 每小时100封
@RateLimiter(key = "ocr:recognize", rate = 50, interval = 60)   // 每分钟50次
```

---

## 7. 部署架构

### 7.1 部署架构图

```
                    ┌─────────────┐
                    │   CDN/OSS   │
                    │  静态资源    │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
   ┌─────────┐       ┌─────────┐       ┌─────────┐
   │ Nginx-1 │       │ Nginx-2 │       │ Nginx-3 │
   │ (LB)    │       │ (LB)    │       │ (LB)    │
   └────┬────┘       └────┬────┘       └────┬────┘
        │                 │                 │
        └─────────┬───────┴─────────────────┘
                  │
    ┌─────────────┼─────────────┐
    │             │             │
    ▼             ▼             ▼
┌───────┐    ┌───────┐    ┌───────┐
│ App-1 │    │ App-2 │    │ App-3 │
│ :8080 │    │ :8080 │    │ :8080 │
└───┬───┘    └───┬───┘    └───┬───┘
    │            │            │
    └─────┬──────┴────────────┘
          │
    ┌─────┴─────┐
    │           │
    ▼           ▼
┌───────┐  ┌───────┐
│ MySQL │  │ Redis │
│Master │  │Cluster│
└───┬───┘  └───────┘
    │
    ▼
┌───────┐
│ MySQL │
│ Slave │
└───────┘
```

### 7.2 Docker Compose配置

```yaml
version: '3.8'

services:
  # Nginx反向代理
  nginx:
    image: nginx:1.24-alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/conf.d:/etc/nginx/conf.d
      - ./nginx/ssl:/etc/nginx/ssl
      - ./frontend/dist:/usr/share/nginx/html
    depends_on:
      - app
    networks:
      - truck-tools-net

  # 后端应用
  app:
    build:
      context: ./backend
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MYSQL_HOST=mysql
      - REDIS_HOST=redis
      - OSS_ENDPOINT=${OSS_ENDPOINT}
      - OCR_API_KEY=${OCR_API_KEY}
    depends_on:
      - mysql
      - redis
    networks:
      - truck-tools-net
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 2G

  # MySQL数据库
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=truck_tools
    volumes:
      - mysql-data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_general_ci
    networks:
      - truck-tools-net

  # Redis缓存
  redis:
    image: redis:7.0-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    command: redis-server --appendonly yes
    networks:
      - truck-tools-net

  # RabbitMQ消息队列
  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      - RABBITMQ_DEFAULT_USER=${RABBITMQ_USER}
      - RABBITMQ_DEFAULT_PASS=${RABBITMQ_PASS}
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    networks:
      - truck-tools-net

volumes:
  mysql-data:
  redis-data:
  rabbitmq-data:

networks:
  truck-tools-net:
    driver: bridge
```

### 7.3 环境配置

#### 7.3.1 开发环境

```yaml
# application-dev.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/truck_tools?useSSL=false&serverTimezone=UTC
    username: root
    password: root123
  redis:
    host: localhost
    port: 6379

logging:
  level:
    root: INFO
    com.trucktools: DEBUG
```

#### 7.3.2 生产环境

```yaml
# application-prod.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST}:3306/truck_tools?useSSL=true
    username: ${MYSQL_USER}
    password: ${MYSQL_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  redis:
    host: ${REDIS_HOST}
    port: 6379
    password: ${REDIS_PASSWORD}

logging:
  level:
    root: WARN
    com.trucktools: INFO
```

---

## 8. 开发规范

### 8.1 代码规范

#### 8.1.1 Java命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | 大驼峰 | `CustomerService` |
| 方法名 | 小驼峰 | `getCustomerById` |
| 常量 | 全大写+下划线 | `MAX_PAGE_SIZE` |
| 包名 | 全小写 | `com.trucktools.customer` |

#### 8.1.2 API响应码规范

| 代码范围 | 说明 |
|----------|------|
| 200 | 成功 |
| 1001-1999 | 认证相关错误 |
| 2001-2999 | 客户模块错误 |
| 3001-3999 | 邮件模块错误 |
| 4001-4999 | OCR模块错误 |
| 5001-5999 | 系统错误 |

### 8.2 Git分支规范

```
main            # 主分支，用于生产环境
├── develop     # 开发分支
├── feature/*   # 功能分支
├── bugfix/*    # 修复分支
├── release/*   # 发布分支
└── hotfix/*    # 紧急修复分支
```

### 8.3 提交信息规范

```
<type>(<scope>): <subject>

类型(type):
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式
- refactor: 重构
- test: 测试
- chore: 构建/工具

示例:
feat(customer): 添加Excel批量导入功能
fix(email): 修复邮件发送超时问题
```

### 8.4 接口文档规范

- 使用Swagger/OpenAPI 3.0规范
- 所有接口必须有完整的请求/响应示例
- 错误码需有详细说明

---

## 附录

### A. 错误码对照表

| 错误码 | 说明 |
|--------|------|
| 1001 | 用户名或密码错误 |
| 1002 | Token已过期 |
| 1003 | 无效的Token |
| 1004 | 账号已被禁用 |
| 2001 | 客户不存在 |
| 2002 | 邮箱已存在 |
| 2003 | 导入文件格式错误 |
| 3001 | SMTP配置错误 |
| 3002 | 邮件发送失败 |
| 3003 | 超出发送限制 |
| 4001 | OCR识别失败 |
| 4002 | 图片格式不支持 |
| 4003 | 图片大小超限 |

### B. 国家代码对照表

参考ISO 3166-1标准

### C. 变更记录

| 版本 | 日期 | 修改人 | 修改内容 |
|------|------|--------|----------|
| v1.0 | 2025-12-02 | [技术负责人] | 初始版本创建 |

---

**文档结束**

