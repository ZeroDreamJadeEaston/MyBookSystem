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

## 📂 目录结构

```text
MyBookSystem/
├── src/main/java/
│   ├── controller/
│   ├── service/
│   ├── mapper/
│   ├── domain/
│   ├── interceptor/
│   └── util/
├── src/main/resources/
├── book_management_frontend/
├── pom.xml
└── sql/
✨ 核心功能
👨‍🎓 读者端
✅ 图书检索：支持书名 / 作者 / ISBN 模糊查询，关键词高亮

✅ 借阅申请：实时库存校验，事务控制防止超借

✅ 个人中心：查看当前借阅状态与历史记录

✅ 智能推荐：展示管理员发布的精选图书

👨‍💼 管理员端
✅ 图书管理：完整 CRUD，支持 AI 一键辅助录入

✅ 借阅监管：查看全馆借阅情况，处理归还与逾期

✅ 用户管理：强制注销校验（需无未还书籍）

✅ 安全机制：邀请码注册 + Token 双重校验

🚀 快速开始
1️⃣ 环境要求
JDK 17+

Node.js 16+（推荐 18.x）

MySQL 8.0+

Redis（默认端口 6379）

2️⃣ 数据库初始化
sql
复制代码
CREATE DATABASE book_db;
