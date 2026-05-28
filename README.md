# 宠物领养救助平台 (Pet Adoption Platform)

> 基于 Spring Boot + Vue 3 的宠物领养综合服务平台，覆盖宠物发布与审核、领养考试与申请、商城购物、即时通讯、AI 助手等全链路功能。

[![Java](https://img.shields.io/badge/Java-17-blue)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3-4FC08D)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-red)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey)](LICENSE)

---

## 项目概述

宠物领养救助平台是一个面向救助站、送养人和领养人的综合服务平台。项目采用前后端分离架构，后端使用 Spring Boot 2.7 多模块 Maven 工程，前端基于 Vite + Vue 3 构建。

平台覆盖了从宠物发布、审核、领养考试、申请审批到志愿者回访的完整业务流程，同时集成了宠物用品商城、即时通讯、AI 智能问答、实名认证等增值功能，致力于打造一站式的宠物领养生态。

### 核心能力

- 宠物全生命周期管理（发布 -> 审核 -> 领养 -> 回访）
- 领养人筛选机制（考试 -> 申请 -> 审批）
- 宠物用品在线商城（商品 -> 购物车 -> 订单 -> 评价）
- 多角色权限体系（普通用户 / 送养人 / 志愿者 / 管理员）
- 即时通讯与 AI 智能助手
- 实名认证与人脸活体检测

---

## 技术栈

### 后端

| 层次 | 技术 |
|------|------|
| 开发语言 | Java 17 |
| 基础框架 | Spring Boot 2.7.18, Spring MVC |
| ORM | MyBatis, PageHelper 分页插件 |
| 数据库 | MySQL 8.0, Redis (Spring Data Redis) |
| 认证授权 | JWT (jjwt 0.12.5), Redis 黑名单机制 |
| API 文档 | Knife4j (Swagger 增强版) |
| 限流 | Bucket4j 令牌桶算法 |
| 即时通讯 | WebSocket, SSE |
| 工具库 | Lombok, Hutool, FastJSON |
| 构建部署 | Maven, Docker |
| 测试 | JUnit 5, Mockito |

### 前端

| 技术 | 用途 |
|------|------|
| Vue 3 | 前端渐进式框架 |
| Vite | 构建工具与开发服务器 |
| Vue Router | 前端路由管理 |
| Pinia | 状态管理 |
| ECharts | 数据可视化图表 |
| ESLint | 代码规范检查 |

### 外部服务

| 服务 | 用途 |
|------|------|
| 高德地图 API | 地址 POI 搜索、逆地理编码 |
| 短信服务 | 手机验证码登录与找回密码 |
| 活体检测 | 人脸识别实名认证 |
| DeepSeek AI | AI 智能问答助手 |
| 滑块验证码 | 注册与敏感操作防刷 |

---

## 模块架构

```
pet (父工程)
├── pet-common            公共模块 — 常量、枚举、异常、统一返回、工具类
├── pet-framework         框架层 — JWT工具、拦截器、全局异常处理、配置
├── pet-gateway           启动入口 — 主启动类、AOP日志、文件上传、仪表盘
├── pet-module-system     系统模块 — 用户、角色、权限、公告、反馈、短信
├── pet-module-pet        宠物模块 — 宠物CRUD、审核、评论、收藏、分类
├── pet-module-adopt      领养模块 — 领养申请、考试、题库、审批
├── pet-module-mall       商城模块 — 商品、购物车、订单、地址、地图
├── pet-module-volunteer  志愿者模块 — 回访记录
├── pet-module-chat       聊天模块 — WebSocket即时通讯、SSE通知
└── pet-module-ai         AI模块 — AI助手对话、会话管理
```

---

## 核心业务流程

### 宠物领养全流程

```
送养人发布宠物 (至少3张图片)
    |
管理员审核 (初审 -> 审核通过 / 打回)
    |
宠物上架展示
    |
领养人浏览 -> 参加领养考试 -> 考试通过
    |
提交领养申请
    |
管理员审批申请 (通过 / 拒绝)
    |
领养成功
    |
志愿者回访 (记录回访情况)
```

### 商城交易流程

```
浏览商品 (分类导航)
    |
加入购物车 / 直接购买
    |
选择收货地址 (支持地图POI搜索)
    |
提交订单 -> 支付
    |
卖家发货
    |
确认收货
    |
评价商品
```

---

## 快速开始

### 前置环境

- JDK 17+
- Apache Maven 3.8+
- MySQL 8.0+
- Redis 6+
- Node.js 18+ (前端开发)
- Docker (可选，用于容器化部署)

### 1. 克隆项目

```bash
git clone https://github.com/your-username/pet-adoption-platform.git
cd pet-adoption-platform
```

### 2. 初始化数据库

```bash
mysql -u root -p < pet_adoption.sql
```

### 3. 配置环境变量

复制环境变量模板并填入真实值：

```bash
cp .env.example .env
```

编辑 `.env` 文件，至少配置以下关键参数：
- `MYSQL_ROOT_PASSWORD` — 数据库密码
- `REDIS_PASSWORD` — Redis 密码
- `JWT_SECRET` — JWT 签名密钥
- 第三方服务 API Key（如无可先留空）

### 4. 配置 Spring Boot

在 `pet-gateway/src/main/resources/` 目录下创建 `application-secret.yml`（已配置到 .gitignore，不会提交到 Git），填入 `.env` 中的敏感信息。

### 5. 启动后端

```bash
mvn clean package -DskipTests
cd pet-gateway/target
java -jar pet-gateway-1.0.0.jar
```

### 6. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 7. 访问系统

- 前端页面: `http://localhost:5173`
- 后端 API: `http://localhost:8080`
- API 文档: `http://localhost:8080/doc.html` (Knife4j)

---

## 项目结构

```
pet-adoption-platform/
├── pet-common/                   # 公共模块
│   └── src/main/java/com/pet/common/
│       ├── constant/             # 常量定义
│       ├── enums/                # 枚举 (领养状态/宠物状态/订单状态/返回码)
│       ├── event/                # 事件定义
│       ├── exception/            # 业务异常
│       ├── result/               # 统一返回封装
│       └── util/                 # 工具类
├── pet-framework/                # 框架层
│   └── src/main/java/com/pet/framework/
│       ├── annotation/           # 自定义注解 (@Log, @RequireRole)
│       ├── config/               # 配置 (CORS/MyBatis/Redis/Swagger/上传)
│       ├── handler/              # 全局异常处理器
│       ├── interceptor/          # 拦截器 (认证/限流)
│       └── util/                 # JWT工具
├── pet-gateway/                  # 启动入口
│   └── src/main/java/com/pet/gateway/
│       ├── aspect/               # AOP日志切面
│       ├── config/               # 启动配置
│       └── controller/           # 文件上传/仪表盘
├── pet-module-system/            # 系统模块
│   └── src/main/java/com/pet/module/system/
│       ├── controller/           # 用户/管理员/公告/反馈/通知/短信
│       ├── mapper/               # MyBatis映射接口
│       ├── model/                # 实体/DTO/VO
│       ├── service/              # 业务逻辑
│       └── event/                # 事件监听器
├── pet-module-pet/               # 宠物模块
│   └── src/main/java/com/pet/module/pet/
│       ├── controller/           # 宠物/审核/评论/收藏/分类
│       └── ...
├── pet-module-adopt/             # 领养模块
│   └── ...
├── pet-module-mall/              # 商城模块
│   └── ...
├── pet-module-volunteer/         # 志愿者模块
│   └── ...
├── pet-module-chat/              # 聊天模块
│   └── ...
├── pet-module-ai/                # AI模块
│   └── ...
├── frontend/                     # 前端工程 (Vue 3 + Vite)
│   ├── src/
│   │   ├── api/                  # API接口封装
│   │   ├── views/                # 页面视图
│   │   │   ├── admin/            # 管理后台
│   │   │   ├── adopt/            # 领养
│   │   │   ├── ai/               # AI助手
│   │   │   ├── auth/             # 认证(登录/注册)
│   │   │   ├── chat/             # 聊天
│   │   │   ├── donate/           # 送养
│   │   │   ├── home/             # 首页
│   │   │   ├── mall/             # 商城
│   │   │   ├── notice/           # 公告
│   │   │   ├── pet/              # 宠物
│   │   │   ├── user/             # 用户中心
│   │   │   └── volunteer/        # 志愿者
│   │   ├── components/           # 公共组件
│   │   ├── composables/          # 组合式函数
│   │   ├── layouts/              # 布局
│   │   ├── router/               # 路由
│   │   ├── stores/               # 状态管理
│   │   └── utils/                # 工具函数
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
├── pet_adoption.sql              # 数据库初始化脚本
├── .env.example                  # 环境变量模板
├── docker-compose.yml            # Docker编排配置
├── pom.xml                       # Maven父工程配置
└── README.md
```

---

## API 规范

所有接口统一返回 `Result<T>` 格式:

```json
{
    "code": 200,
    "msg": "操作成功",
    "data": { ... }
}
```

错误码分类:
- `200` — 成功
- `400x` — 参数错误
- `401x` — 认证错误
- `403x` — 权限错误
- `404x` — 资源不存在
- `500x` — 服务端错误

详细接口文档请启动项目后访问 Knife4j: `http://localhost:8080/doc.html`

---

## 安全设计

- JWT 认证 + Redis Token 黑名单（退出即失效）
- 用户被禁用时自动拉黑所有 Token
- 管理员权限隔离，无法执行普通用户操作
- 角色权限注解 `@RequireRole`，数据库实时校验
- 敏感接口限流（Bucket4j 令牌桶）
- 密码加密存储
- CORS 跨域白名单配置化
- 全局异常处理，不泄露堆栈信息

---

## 配置文件说明

| 文件 | 用途 | 是否提交 Git |
|------|------|-------------|
| `application.yml` | 公共配置 | 是 |
| `application-secret.yml` | 敏感配置 (API Key / 密码) | 否 (.gitignore) |
| `.env.example` | 环境变量模板 | 是 |
| `.env` | 本地环境变量 | 否 (.gitignore) |

---

## 开发指南

### 分支管理

- `main` — 稳定主分支
- `develop` — 开发分支
- `feature/*` — 功能分支
- `hotfix/*` — 紧急修复分支

### 提交规范

请参考 [CONTRIBUTING.md](CONTRIBUTING.md) 中的提交信息规范。

---

## License

[MIT License](LICENSE)

---

## 联系方式

- 项目地址: [GitHub](https://github.com/PriestZZb/pet-adoption-platform)
- 如有问题请提交 [Issue](https://github.com/PriestZZb/pet-adoption-platform/issues)
