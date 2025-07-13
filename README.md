# 学链智联系统 (EduChain)

[![Java](https://img.shields.io/badge/Java-8-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.2.25-brightgreen.svg)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-4.5.4-blue.svg)](https://www.typescriptlang.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-Latest-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## 📖 项目简介

学链智联系统是一个基于Spring Boot + Vue 3构建的高性能伙伴匹配平台。系统提供用户管理、智能标签匹配、组队协作、实时聊天等核心功能，帮助用户找到志同道合的学习伙伴和团队成员。

### 🎯 核心功能

- **🔐 用户管理系统**：注册登录、信息维护、权限控制
- **🏷️ 智能标签匹配**：基于标签的用户推荐算法
- **👥 组队协作**：创建队伍、邀请成员、权限管理
- **💬 实时消息通信**：聊天功能、消息推送
- **📊 数据分析**：用户行为分析、匹配效果统计
- **📱 移动端优化**：响应式设计、移动端适配

## 🏗️ 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│                 │    │                 │    │                 │
│   前端应用       │────│   后端服务       │────│   数据存储       │
│                 │    │                 │    │                 │
│  Vue 3 + TS     │    │  Spring Boot    │    │  MySQL + Redis  │
│  Vant UI        │    │  MyBatis-Plus   │    │  Redisson       │
│  Vite           │    │  Redis Cache    │    │                 │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🛠️ 技术选型

### 后端技术栈
- **Spring Boot 2.6.4** - 企业级Java开发框架
- **MyBatis-Plus** - 增强版MyBatis持久层框架
- **Redis + Redisson** - 分布式缓存与分布式锁
- **MySQL 8.0** - 关系型数据库
- **Swagger + Knife4j** - API文档生成
- **MybatisX** - 自动生成代码
- **Spring Scheduler** - 定时任务
- **Gson** - JSON 序列化库
- 相似度匹配算法

### 前端技术栈
- **Vue 3.2.25** - 渐进式JavaScript框架
- **TypeScript 4.5.4** - 类型安全的JavaScript超集
- **Vant 3.4.8** - 移动端Vue组件库
- **Vite 2.9.5** - 新一代前端构建工具
- **Axios** - HTTP客户端

## 📱 界面展示

### 主要功能截图

[**首页 - 用户推荐**](https://github.com/mumuzi6/educhain-source/blob/main/image/首页心动模式.png)

---

[**搜索功能 - 标签筛选**](https://github.com/mumuzi6/educhain-source/blob/main/image/标签搜索.png)

---

[**队伍管理 - 创建**](https://github.com/mumuzi6/educhain-source/blob/main/image/创建队伍.png)
[**队伍管理 - 加入**](https://github.com/mumuzi6/educhain-source/blob/main/image/找队伍.png)

---

[**实时聊天列表**](https://github.com/mumuzi6/educhain-source/blob/main/image/消息通信.png)

[**个人消息通信**](https://github.com/mumuzi6/educhain-source/blob/main/image/个人私信.png)

---

[**个人中心**](https://github.com/mumuzi6/educhain-source/blob/main/image/用户中心.png)

[**更新信息**](https://github.com/mumuzi6/educhain-source/blob/main/image/更新信息.png)

---


## 🚀 快速开始

### 环境要求
- Java 8+
- Node.js 14+
- Maven 3.6+
- MySQL 5.7+
- Redis 3.0+

### 后端启动

1. **克隆项目**
   ```bash
   git clone https://github.com/mumuzi6/educhain-source.git
   cd educhain-source/educhain-backend-master/educhain-backend
   ```

2. **配置数据库**
   ```bash
   # 创建数据库
   mysql -u root -p
   CREATE DATABASE educhain_db;
   
   # 导入表结构
   mysql -u root -p educhain_db < sql/create_table.sql
   ```

3. **修改配置**
   ```yaml
   # application.yml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/educhain_db
       username: your_username
       password: your_password
     redis:
       host: localhost
       port: 6379
   ```

4. **启动应用**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### 前端启动

1. **进入前端目录**
   ```bash
   cd educhain-source/educhain-frontend-master/educhain-frontend-master
   ```

2. **安装依赖**
   ```bash
   npm install
   # 或
   yarn install
   ```

3. **启动开发服务器**
   ```bash
   npm run dev
   # 或
   yarn dev
   ```

4. **访问应用**
   - 前端应用：http://localhost:3000
   - 后端API文档：http://localhost:8080/api/doc.html

## 📁 项目结构

```
educhain-source/
├── educhain-backend-master/     # 后端项目
│   └── educhain-backend/
│       ├── src/main/java/com/kryos/educhain/
│       │   ├── controller/      # 控制器层
│       │   ├── service/         # 业务逻辑层
│       │   ├── mapper/          # 数据访问层
│       │   ├── model/           # 数据模型
│       │   ├── config/          # 配置类
│       │   └── utils/           # 工具类
│       ├── src/main/resources/  # 配置文件
│       └── sql/                 # 数据库脚本
├── educhain-frontend-master/    # 前端项目
│   └── educhain-frontend-master/
│       ├── src/
│       │   ├── pages/           # 页面组件
│       │   ├── components/      # 通用组件
│       │   ├── services/        # API服务
│       │   ├── utils/           # 工具函数
│       │   └── models/          # 类型定义
│       ├── public/              # 静态资源
│       └── package.json         # 依赖配置
└── README.md                    # 项目说明
```

## 🔧 核心特性

### 后端特性
- **分布式缓存架构**：多级缓存策略 + 缓存预热
- **高并发处理**：分布式锁 + 线程池优化
- **智能匹配算法**：基于标签权重的用户推荐
- **安全认证**：JWT + 权限控制
- **性能优化**：数据库索引 + 批量处理

### 前端特性
- **响应式设计**：移动优先 + 自适应布局
- **性能优化**：路由懒加载 + 组件缓存
- **用户体验**：Loading状态 + 错误处理
- **现代化UI**：Vant组件库 + 主题切换

## 📊 性能指标

- **QPS**：单机支持1000+ QPS
- **响应时间**：平均响应时间 < 100ms
- **并发用户**：支持10,000+在线用户
- **数据规模**：支持百万级用户数据

## 🧪 测试

```bash
# 后端测试
cd educhain-backend-master/educhain-backend
mvn test

# 前端测试
cd educhain-frontend-master/educhain-frontend-master
npm run test
```

## 📦 部署

### Docker部署
```bash
# 构建后端镜像
cd educhain-backend-master/educhain-backend
docker build -t educhain-backend .

# 构建前端镜像
cd educhain-frontend-master/educhain-frontend-master
docker build -t educhain-frontend .

# 启动服务
docker-compose up -d
```

### 传统部署
- **后端**：打包为jar文件，部署到服务器
- **前端**：构建静态文件，部署到Nginx

## 🤝 贡献指南

1. Fork 项目到个人仓库
2. 创建新的功能分支 (`git checkout -b feature/新功能`)
3. 提交代码更改 (`git commit -m '添加新功能'`)
4. 推送到功能分支 (`git push origin feature/新功能`)
5. 创建 Pull Request

### 代码规范
- 遵循SOLID原则
- 每个函数只干一件事
- 所有异常必须处理
- 变量名要说人话

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

- 项目维护者：mumuzi6
- 邮箱：2679161834@qq.com
- 项目地址：[mumuzi6/educhain-source](https://github.com/mumuzi6/educhain-source)

---

⭐ 如果这个项目对你有帮助，请给我一个星标！ 