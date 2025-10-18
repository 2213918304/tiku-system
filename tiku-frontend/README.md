# 题库学习系统 - 前端项目

## 🎨 项目特色

- ✨ **现代化UI设计** - 采用渐变色、毛玻璃、卡片式设计
- 🚀 **Vue 3 + TypeScript** - 类型安全的现代前端开发
- 🎯 **Element Plus** - 专业的组件库
- 📱 **响应式布局** - 完美支持PC和移动端
- 🎭 **流畅动画** - 优雅的过渡和交互效果

## 🛠️ 技术栈

- **框架**: Vue 3.5 + TypeScript
- **构建工具**: Vite 7
- **UI框架**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **图表**: ECharts
- **样式**: SCSS
- **工具库**: @vueuse/core, dayjs

## 📦 安装依赖

```bash
npm install
```

## 🚀 启动开发服务器

```bash
npm run dev
```

访问: http://localhost:5173

## 🔧 编译生产版本

```bash
npm run build
```

## 📁 项目结构

```
tiku-frontend/
├── src/
│   ├── api/                # API接口
│   │   ├── modules/        # 模块化API
│   │   └── request.ts      # Axios配置
│   ├── assets/             # 静态资源
│   ├── components/         # 公共组件
│   ├── layout/             # 布局组件
│   │   └── MainLayout.vue  # 主布局
│   ├── router/             # 路由配置
│   ├── stores/             # Pinia状态管理
│   │   ├── app.ts          # 应用状态
│   │   └── user.ts         # 用户状态
│   ├── styles/             # 全局样式
│   │   ├── variables.scss  # SCSS变量
│   │   └── index.scss      # 全局样式
│   ├── types/              # TypeScript类型
│   ├── views/              # 页面组件
│   │   ├── auth/           # 登录注册
│   │   ├── dashboard/      # 首页
│   │   ├── practice/       # 刷题页面
│   │   ├── subjects/       # 题库页面
│   │   ├── collection/     # 收藏页面
│   │   ├── notes/          # 笔记页面
│   │   ├── wrong/          # 错题本页面
│   │   ├── statistics/     # 统计页面
│   │   ├── ranking/        # 排行榜页面
│   │   └── profile/        # 个人中心
│   ├── App.vue             # 根组件
│   └── main.ts             # 入口文件
├── public/                 # 公共资源
├── index.html              # HTML模板
├── vite.config.ts          # Vite配置
├── tsconfig.json           # TypeScript配置
└── package.json            # 项目依赖

```

## 🎯 功能模块

### 用户系统
- ✅ 登录/注册
- ✅ JWT认证
- ✅ 用户信息管理

### 刷题系统
- ✅ 9种刷题模式
  - 随机刷题
  - 顺序刷题
  - 章节练习
  - 考试模拟
  - 错题强化
  - 收藏专练
  - 闯关模式
  - 限时挑战
  - 智能推荐
- ✅ 答题界面
- ✅ 实时判题

### 学习管理
- ✅ 题目收藏
- ✅ 学习笔记
- ✅ 错题本

### 数据统计
- ✅ 学习统计
- ✅ 学习日历
- ✅ 排行榜
- ✅ 数据可视化

## 🎨 UI设计亮点

### 1. 登录页
- 渐变背景动画
- 毛玻璃效果
- 流畅的表单过渡
- 快速登录选项

### 2. 主界面
- 侧边栏可折叠
- 现代化导航
- 卡片式布局
- 响应式设计

### 3. Dashboard
- 统计卡片
- 渐变色背景
- 快捷入口
- 数据可视化

### 4. 刷题页面
- 卡片式选择
- Hover动画
- 模式说明
- 一键开始

## 🔌 API代理配置

开发环境下，API请求会自动代理到后端服务器：

```typescript
// vite.config.ts
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

## 🎯 环境变量

创建 `.env.local` 文件配置环境变量：

```env
VITE_API_BASE_URL=http://localhost:8080
```

## 📝 开发规范

### 组件命名
- 使用PascalCase命名组件
- 文件名与组件名一致

### 代码风格
- 使用ESLint + Prettier
- TypeScript严格模式
- 遵循Vue 3风格指南

### Git提交
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 样式调整
- refactor: 重构
- test: 测试相关
- chore: 构建配置

## 🐛 常见问题

### 1. 安装依赖失败
```bash
# 清除npm缓存
npm cache clean --force
# 重新安装
npm install
```

### 2. 启动失败
- 检查Node版本 (需要 >= 18)
- 检查端口占用 (5173)
- 查看错误日志

### 3. API请求失败
- 确认后端服务已启动 (8080端口)
- 检查代理配置
- 查看浏览器控制台错误

## 📚 相关文档

- [Vue 3](https://cn.vuejs.org/)
- [Vite](https://cn.vitejs.dev/)
- [Element Plus](https://element-plus.org/zh-CN/)
- [Pinia](https://pinia.vuejs.org/zh/)
- [Vue Router](https://router.vuejs.org/zh/)

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

## 📄 许可证

MIT License

---

**✨ 享受现代化的学习体验！**
