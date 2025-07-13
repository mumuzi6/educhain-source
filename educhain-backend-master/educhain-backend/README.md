# 学链智联系统 - 后端服务

[![Java](https://img.shields.io/badge/Java-8-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-Latest-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## 📖 项目简介

学链智联系统后端服务，基于Spring Boot构建的高性能伙伴匹配平台。提供用户管理、标签匹配、组队协作、智能推荐等核心功能的RESTful API服务。

### 🎯 核心功能

- **用户管理系统**：注册登录、信息维护、权限控制
- **智能标签匹配**：基于标签的用户推荐算法
- **组队协作**：创建队伍、邀请成员、权限管理
- **消息通信**：实时聊天、消息推送
- **数据分析**：用户行为分析、匹配效果统计

### 🏗️ 系统架构

本项目采用经典的分层架构设计，涵盖了企业级开发的各种技术场景：

- **分布式缓存**：Redis集群 + 缓存预热策略
- **并发控制**：Redisson分布式锁 + 线程池优化
- **数据处理**：MyBatis-Plus + 批量导入优化
- **性能优化**：缓存策略 + 异步处理 + 数据库索引优化
- **监控运维**：接口文档 + 异常处理 + 日志管理



## 🛠️ 技术选型

### 核心框架
- **Spring Boot 2.6.4** - 企业级Java开发框架
- **Spring Security** - 安全认证授权框架
- **Spring Data Redis** - Redis数据访问抽象层
- **MyBatis-Plus** - 增强版MyBatis持久层框架

### 数据存储
- **MySQL 8.0** - 关系型数据库
- **Redis 6.x** - 内存数据库缓存
- **Redisson** - Redis分布式锁客户端

### 开发工具
- **MyBatis X** - 代码自动生成插件
- **Easy Excel** - Excel数据处理库
- **Swagger + Knife4j** - API文档生成工具
- **Gson** - JSON序列化处理库

### 任务调度
- **Spring Scheduler** - 定时任务框架
- **线程池** - 异步任务处理

### 部署运维
- **Docker** - 容器化部署
- **Maven** - 项目构建管理
- **Logback** - 日志管理框架



## 🚀 快速开始

### 环境要求
- Java 8+
- Maven 3.6+
- MySQL 5.7+
- Redis 3.0+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/mumuzi6/educhain-backend.git
   cd educhain-backend
   ```

2. **配置数据库**
   ```bash
   # 创建数据库
   mysql -u root -p
   CREATE DATABASE educhain_db;
   
   # 导入表结构
   mysql -u root -p educhain_db < sql/create_table.sql
   ```

3. **配置Redis**
   ```bash
   # 启动Redis服务
   redis-server
   ```

4. **修改配置文件**
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

5. **启动应用**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

6. **访问接口文档**
   
   ```
   http://localhost:8080/api/doc.html
   ```

## 📁 项目结构

```
src/main/java/com/kryos/educhain/
├── common/           # 通用响应类和工具
├── config/           # 配置类
├── constant/         # 常量定义
├── controller/       # 控制器层
├── exception/        # 异常处理
├── job/             # 定时任务
├── mapper/          # 数据访问层
├── model/           # 数据模型
│   ├── domain/      # 实体类
│   ├── dto/         # 数据传输对象
│   ├── enums/       # 枚举类
│   ├── request/     # 请求参数
│   └── vo/          # 视图对象
├── service/         # 业务逻辑层
├── utils/           # 工具类
└── MyApplication.java # 启动类
```

## 🔧 核心特性

### 分布式缓存架构
- **多级缓存策略**：本地缓存 + Redis分布式缓存
- **缓存预热机制**：定时任务预加载热点数据
- **缓存一致性**：采用Cache-Aside模式保证数据一致性

### 高并发处理
- **分布式锁**：基于Redisson实现分布式锁机制
- **线程池优化**：自定义线程池处理异步任务
- **数据库连接池**：HikariCP高性能连接池

### 算法优化
- **相似度匹配算法**：基于标签权重的用户推荐
- **数据批量处理**：优化大数据量导入性能
- **SQL优化**：添加合理索引提升查询性能

## 📊 性能指标

- **QPS**：单机支持1000+ QPS
- **响应时间**：平均响应时间 < 100ms
- **并发用户**：支持10,000+在线用户
- **数据规模**：支持百万级用户数据

## 🔐 安全特性

- **JWT认证**：无状态Token认证机制
- **权限控制**：基于角色的访问控制(RBAC)
- **数据校验**：完善的入参校验和异常处理
- **SQL防注入**：MyBatis-Plus预编译SQL

## 📚 API文档

项目集成了Swagger3 + Knife4j，提供完整的API文档：

- **在线文档**：`http://localhost:8080/api/doc.html`
- **API测试**：支持在线接口测试
- **参数说明**：详细的请求响应参数说明

## 🧪 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn integration-test

# 生成测试报告
mvn surefire-report:report
```

## 📈 监控运维

- **健康检查**：`/actuator/health`
- **应用信息**：`/actuator/info`
- **性能指标**：`/actuator/metrics`
- **日志管理**：基于Logback的分级日志

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

- 项目维护者：mumuzi6
- 邮箱：2679161834@qq.com
- 项目地址：[mumuzi6/educhain-source](https://github.com/mumuzi6/educhain-source)



⭐ 如果这个项目对你有帮助，请给我一个星标！

