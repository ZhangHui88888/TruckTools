# TruckTools Backend

卡车外贸综合工具平台 - 后端服务

## 技术栈

- **Java 17 LTS**
- **Spring Boot 3.2.5**
- **Spring Security 6.x** - 安全框架
- **MyBatis-Plus 3.5.5** - ORM框架
- **MySQL 8.0+** - 数据库
- **Redis 7.0+** - 缓存/会话
- **JWT** - 身份认证

## 项目结构

```
TruckToolsBackend/
├── truck-tools-common/       # 公共模块 - 工具类、常量、异常处理
├── truck-tools-system/       # 系统模块 - 用户管理、认证授权、SMTP配置
├── truck-tools-customer/     # 客户模块 - 客户管理、名片识别、Excel导入
├── truck-tools-email/        # 邮件模块 - 邮件模板、批量发送、发送日志
├── truck-tools-admin/        # 启动模块 - 应用入口、配置文件
└── sql/                      # SQL脚本
    └── init.sql              # 数据库初始化脚本
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+

### 2. 数据库初始化

```bash
# 登录MySQL并执行初始化脚本
mysql -u root -p < sql/init.sql
```

### 3. 修改配置

编辑 `truck-tools-admin/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/truck_tools?...
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

### 4. 编译运行

```bash
# 编译项目
mvn clean package -DskipTests

# 运行应用
cd truck-tools-admin
mvn spring-boot:run

# 或者直接运行jar
java -jar target/truck-tools-admin-1.0.0.jar
```

### 5. 访问应用

- API文档: http://localhost:8080/doc.html
- 健康检查: http://localhost:8080/actuator/health

## API接口

### 认证模块 `/api/v1/auth`

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /login | 用户登录 |
| POST | /register | 用户注册 |
| POST | /refresh | 刷新Token |
| POST | /logout | 退出登录 |

### 用户模块 `/api/v1/user`

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /profile | 获取个人资料 |
| PUT | /profile | 更新个人资料 |
| PUT | /password | 修改密码 |

### 客户模块 `/api/v1/customers`

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 获取客户列表 |
| GET | /{id} | 获取客户详情 |
| POST | / | 创建客户 |
| PUT | /{id} | 更新客户 |
| DELETE | /{id} | 删除客户 |
| POST | /export | 导出客户 |

### 名片模块 `/api/v1/business-cards`

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /upload | 上传名片 |
| GET | /{id} | 获取识别结果 |
| GET | /batch/{batchId}/status | 批次状态 |
| POST | /{id}/confirm | 确认转客户 |

### 邮件模板 `/api/v1/email/templates`

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 模板列表 |
| POST | / | 创建模板 |
| PUT | /{id} | 更新模板 |
| DELETE | /{id} | 删除模板 |
| GET | /variables | 可用变量 |
| POST | /preview | 预览邮件 |

### 邮件任务 `/api/v1/email/tasks`

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | / | 创建任务 |
| GET | / | 任务列表 |
| GET | /{taskId} | 任务详情 |
| POST | /{taskId}/start | 开始发送 |
| POST | /{taskId}/pause | 暂停 |
| POST | /{taskId}/resume | 继续 |
| POST | /{taskId}/cancel | 取消 |
| GET | /{taskId}/logs | 发送日志 |

### SMTP配置 `/api/v1/settings/smtp`

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 配置列表 |
| POST | / | 创建配置 |
| PUT | /{id} | 更新配置 |
| DELETE | /{id} | 删除配置 |
| POST | /{id}/test | 测试连接 |

## 开发指南

### 代码规范

- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 统一响应格式 `Result<T>`

### 分支管理

```
main          # 主分支，生产环境
├── develop   # 开发分支
├── feature/* # 功能分支
└── hotfix/*  # 紧急修复
```

### 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

## 部署

### Docker部署

```bash
# 构建镜像
docker build -t truck-tools-backend .

# 运行容器
docker run -d -p 8080:8080 \
  -e MYSQL_HOST=mysql \
  -e REDIS_HOST=redis \
  truck-tools-backend
```

### Docker Compose

```bash
docker-compose up -d
```

## 许可证

MIT License

