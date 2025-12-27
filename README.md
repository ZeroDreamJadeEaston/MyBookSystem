[![Typing SVG](https://readme-typing-svg.herokuapp.com?font=cascadia+code&size=35&duration=3000&pause=1000&color=00ADFF&center=true&vCenter=true&random=false&width=1000&height=80&lines=Book+Lending+Management+System;基于+Spring+Boot+3+与+Vue+3+的图书借阅管理系统)](https://git.io/typing-svg)

<div align="center">

![SpringBoot](https://img.shields.io/badge/SpringBoot-v3.0+-6DB33F?style=flat&logo=springboot&logoColor=white)
![MyBatisPlus](https://img.shields.io/badge/MyBatisPlus-v3.5+-00547D?style=flat&logo=mybatis&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-v8.0-4479A1?style=flat&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-v6.0+-DC382D?style=flat&logo=redis&logoColor=white)
![Vue3](https://img.shields.io/badge/Vue-v3.0+-4FC08D?style=flat&logo=vue.js&logoColor=white)
![ElementPlus](https://img.shields.io/badge/ElementPlus-v2.3+-409EFF?style=flat&logo=element&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-v4.0+-646CFF?style=flat&logo=vite&logoColor=white)

</div>

<br/>

> [!TIP]
> **项目简介**
>
> 本项目是一套基于 **B/S 架构** 的现代化图书借阅管理系统。
> 采用 **前后端分离** 模式开发，集成了 **DeepSeek AI** 大模型接口用于图书信息的自动辅助录入。系统包含完整的权限管理（读者/管理员）、借阅流转闭环、以及高性能的缓存策略，适合作为课程设计或毕业设计参考。

## 🛠️ 技术栈 (Tech Stack)

### 💻 后端 (Backend)
- **核心框架**：Spring Boot 3
- **ORM 框架**：MyBatis-Plus (支持 LambdaQueryWrapper)
- **数据库**：MySQL 8.0
- **缓存中间件**：Redis (用于 Token 存储、防雪崩缓存策略)
- **安全鉴权**：JWT (JSON Web Token) + 拦截器 (`JwtTokenInterceptor`)
- **AI 能力**：集成 DeepSeek API (自动生成图书简介与分类)
- **工具库**：Lombok, FastJson

### 🎨 前端 (Frontend)
- **框架**：Vue 3 (Composition API)
- **构建工具**：Vite
- **UI 组件库**：Element Plus
- **状态管理**：Pinia
- **HTTP 客户端**：Axios

---

## 📂 目录结构 (Directory Structure)

> [!NOTE]
> 本项目采用 Monorepo 结构，前后端代码存放于同一仓库中。

```text
MyBookSystem/
├── src/main/java/          # ☕ 后端 Java 源代码
│   ├── controller/         # 控制层 (BookController, AdminController...)
│   ├── service/            # 业务层 (借阅逻辑, AI 调用...)
│   ├── mapper/             # 持久层接口
│   ├── domain/             # 实体类 (Entity, VO, DTO)
│   ├── interceptor/        # 拦截器 (JwtTokenInterceptor)
│   └── util/               # 工具类 (JwtUtils, MyUtils)
├── src/main/resources/     # ⚙️ 后端配置文件 & Mapper XML
├── book_management_frontend/ # 🎨 前端 Vue 项目源码
├── pom.xml                 # 📦 Maven 依赖管理
└── sql/                    # 🗄️ 数据库 SQL 脚本
```
## ✨ 核心功能 (Features)

### 👨‍🎓 读者端 (Reader)
- [x] **图书检索**：支持按书名、作者、ISBN 模糊搜索，关键词高亮显示。
- [x] **借阅申请**：实时库存检测，事务控制防止超卖。
- [x] **个人中心**：查看当前借阅状态、历史归还记录。
- [x] **智能推荐**：查看管理员发布的精选图书推荐。

### 👨‍💼 管理员端 (Admin)
- [x] **图书管理**：CRUD 操作，支持 **✨ AI 一键辅助录入**。
- [x] **借阅监管**：查看全馆借阅数据，处理还书与逾期。
- [x] **用户管理**：管理读者账号，包含强制注销逻辑（需无未还书籍）。
- [x] **安全机制**：注册邀请码校验，Token 双重认证。

---

## 🚀 快速开始 (Getting Started)

### 1. 环境准备
确保本地已安装：
- **JDK**: 17+
- **Node.js**: 16+ (推荐 18.x)
- **MySQL**: 8.0+
- **Redis**: 必须启动服务（默认端口 6379）

### 2. 数据库配置
1. 创建数据库 `book_db`。
2. 运行项目根目录下的 SQL 脚本，初始化表结构与数据。
3. 修改 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/book_db?serverTimezone=Asia/Shanghai
    username: root  # 修改为你的数据库账号
    password: root  # 修改为你的数据库密码
  data:
    redis:
      host: localhost
      port: 6379
```

### 3. 启动后端
1. 使用 IDEA 打开项目根目录。
2. 等待 Maven 依赖下载完成。
3. 运行 `Application.java`。
4. 服务默认运行在 `http://localhost:8080`。

### 4. 启动前端
打开终端，进入前端目录：

```bash
# 1. 进入前端目录
cd book_management_frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev
```
访问地址：http://localhost:5173


## ⚠️ 注意事项 (Notes)

> [!IMPORTANT]
> **关于 Redis**
> 项目强依赖 Redis 进行 Token 校验与数据缓存。**启动项目前请务必开启 Redis 服务**，否则登录功能将报错 `RedisConnectionFailure`。

> [!WARNING]
> **关于 AI 接口**
> AI 辅助录入功能依赖 DeepSeek API，请确保网络通畅，并在配置文件中检查 API Key 是否有效。
