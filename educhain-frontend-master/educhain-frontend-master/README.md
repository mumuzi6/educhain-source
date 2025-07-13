# 学链智联系统 - 前端应用

[![Vue.js](https://img.shields.io/badge/Vue.js-3.2.25-brightgreen.svg)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-4.5.4-blue.svg)](https://www.typescriptlang.org/)
[![Vant](https://img.shields.io/badge/Vant-3.4.8-07c160.svg)](https://vant-contrib.gitee.io/vant/)
[![Vite](https://img.shields.io/badge/Vite-2.9.5-646CFF.svg)](https://vitejs.dev/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## 📖 项目简介

学链智联系统前端应用，采用Vue 3 + TypeScript + Vant UI构建的移动端伙伴匹配平台。提供用户注册登录、智能匹配、组队协作、实时聊天等功能的现代化移动端界面。

### 🎯 核心功能

- **🔐 用户系统**：注册登录、个人信息管理、头像上传
- **🔍 智能搜索**：按标签搜索用户、条件筛选、推荐算法
- **👥 组队协作**：创建队伍、加入队伍、队伍管理
- **💬 实时聊天**：消息发送、聊天记录、在线状态
- **📱 移动优化**：响应式设计、触摸优化、离线缓存

### ✨ 界面预览

- **主页展示**：个性化推荐、热门标签、用户动态
- **搜索功能**：智能搜索、筛选条件、搜索历史
- **组队模块**：队伍列表、创建队伍、邀请管理
- **聊天界面**：实时消息、表情包、文件分享
- **个人中心**：资料编辑、设置管理、数据统计

## 🛠️ 技术选型

### 核心框架
- **Vue 3.2.25** - 渐进式JavaScript框架，采用Composition API
- **TypeScript 4.5.4** - 类型安全的JavaScript超集
- **Vite 2.9.5** - 新一代前端构建工具，快速热更新
- **Vue Router 4** - Vue.js官方路由管理器

### UI组件库
- **Vant 3.4.8** - 轻量、可靠的移动端Vue组件库
- **Vant Icons** - 丰富的图标组件
- **CSS3** - 现代CSS特性，支持弹性布局和动画

### 工具库
- **Axios 0.27.2** - 基于Promise的HTTP客户端
- **Day.js 1.11.13** - 轻量级日期处理库
- **QS 6.10.5** - 查询字符串解析库
- **Vue Devtools** - Vue开发调试工具

## 🚀 快速开始

### 环境要求
- Node.js 14+
- npm 6+ 或 yarn 1.22+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/mumuzi6/educhain-source/tree/main/educhain-frontend-master
   cd educhain-frontend
   ```

2. **安装依赖**
   ```bash
   # 使用npm
   npm install
   
   # 或使用yarn
   yarn install
   ```

3. **配置环境**
   ```bash
   # 复制环境配置文件
   cp .env.example .env.local
   
   # 修改API接口地址
   VITE_API_BASE_URL=http://localhost:8080/api
   ```

4. **启动开发服务器**
   ```bash
   # 使用npm
   npm run dev
   
   # 或使用yarn
   yarn dev
   ```

5. **访问应用**
   ```
   http://localhost:3000
   ```

## 📁 项目结构

```
src/
├── assets/           # 静态资源
│   ├── images/      # 图片资源
│   └── styles/      # 全局样式
├── components/       # 通用组件
│   ├── TeamCardList.vue    # 队伍卡片列表
│   ├── UserCardList.vue    # 用户卡片列表
│   └── TeamInvite.vue      # 队伍邀请组件
├── config/           # 配置文件
│   └── route.ts     # 路由配置
├── constants/        # 常量定义
│   ├── team.ts      # 队伍相关常量
│   └── user.ts      # 用户相关常量
├── layouts/          # 布局组件
│   └── BasicLayout.vue     # 基础布局
├── models/           # 类型定义
│   ├── baseResponse.d.ts   # 基础响应类型
│   ├── team.d.ts          # 队伍类型
│   └── user.d.ts          # 用户类型
├── pages/            # 页面组件
│   ├── Index.vue            # 首页
│   ├── SearchPage.vue       # 搜索页面
│   ├── TeamPage.vue         # 队伍页面
│   ├── ChatPage.vue         # 聊天页面
│   └── UserPage.vue         # 用户页面
├── plugins/          # 插件配置
│   └── myAxios.ts   # Axios配置
├── services/         # API服务
│   ├── user.ts      # 用户API
│   └── message.ts   # 消息API
├── states/           # 状态管理
│   └── user.ts      # 用户状态
├── utils/            # 工具函数
│   ├── tagUtils.ts  # 标签工具
│   └── timeUtils.ts # 时间工具
├── App.vue           # 根组件
└── main.ts           # 应用入口
```

## 🔧 核心特性

### 响应式设计
- **移动优先**：专为移动设备优化的界面设计
- **自适应布局**：支持不同屏幕尺寸的设备
- **触摸优化**：针对触摸操作优化的交互体验

### 性能优化
- **路由懒加载**：按需加载页面组件，减少初始包大小
- **图片懒加载**：延迟加载图片，提升页面加载速度
- **组件缓存**：合理使用keep-alive缓存组件状态
- **打包优化**：Vite构建优化，支持Tree Shaking

### 用户体验
- **加载状态**：完善的Loading和Skeleton屏幕
- **错误处理**：友好的错误提示和重试机制
- **离线支持**：基础的离线缓存功能
- **主题切换**：支持明暗主题切换

## 📱 页面说明

### 首页 (Index.vue)
- 个性化推荐用户列表
- 热门标签快速筛选
- 搜索入口和导航

### 搜索页面 (SearchPage.vue)
- 按标签搜索用户
- 多条件筛选功能
- 搜索结果展示

### 队伍页面 (TeamPage.vue)
- 队伍列表展示
- 创建和加入队伍
- 队伍详情管理

### 聊天页面 (ChatPage.vue)
- 实时消息收发
- 聊天记录查看
- 消息状态显示

### 用户页面 (UserPage.vue)
- 个人信息展示
- 资料编辑功能
- 设置和退出

## 🎨 UI设计规范

### 色彩规范
- **主色**：#1989fa (蓝色)
- **成功色**：#07c160 (绿色)
- **警告色**：#ff976a (橙色)
- **危险色**：#ee0a24 (红色)

### 字体规范
- **主字体**：PingFang SC, Helvetica Neue, Arial, sans-serif
- **字号**：12px, 14px, 16px, 18px, 20px
- **行高**：1.5倍字号

### 间距规范
- **基础间距**：4px的倍数 (4px, 8px, 12px, 16px, 20px, 24px)
- **组件间距**：16px
- **页面边距**：16px

## 🧪 测试

```bash
# 运行单元测试
npm run test:unit

# 运行端到端测试
npm run test:e2e

# 生成测试覆盖率报告
npm run test:coverage
```

## 📦 构建部署

```bash
# 构建生产版本
npm run build

# 预览构建结果
npm run preview

# 静态文件分析
npm run analyze
```

### 部署配置

```nginx
# Nginx配置示例
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/educhain-frontend/dist;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api/ {
        proxy_pass http://backend-server:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 🔌 API集成

### 请求配置
```typescript
// plugins/myAxios.ts
const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  withCredentials: true
});
```

### 接口规范
- **基础路径**：`/api`
- **认证方式**：Cookie/Session
- **数据格式**：JSON
- **错误处理**：统一错误码和消息

## 🤝 贡献指南

1. Fork 项目到个人仓库
2. 创建新的功能分支 (`git checkout -b feature/新功能`)
3. 提交代码更改 (`git commit -m '添加新功能'`)
4. 推送到功能分支 (`git push origin feature/新功能`)
5. 创建 Pull Request

### 代码规范
- 使用 ESLint + Prettier 进行代码格式化
- 遵循 Vue 3 Composition API 最佳实践
- 组件命名采用 PascalCase
- 文件命名采用 kebab-case

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

- 项目维护者：mumuzi6
- 邮箱：2679161834@qq.com
- 项目地址：[mumuzi6/educhain-source](https://github.com/mumuzi6/educhain-source)

---

⭐ 如果这个项目对你有帮助，请给我一个星标！
