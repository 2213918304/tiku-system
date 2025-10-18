# 🚀 辽师大题库系统 - 一键部署指南

## 📋 目录
- [本地部署（推荐新手）](#本地部署)
- [Railway部署（推荐）](#railway部署)
- [Render部署](#render部署)
- [Zeabur部署](#zeabur部署)
- [常见问题](#常见问题)

---

## 🖥️ 本地部署

### 前置要求
- ✅ Docker Desktop
- ✅ Git

### 一键部署

**Windows用户：**
```bash
双击运行 deploy.bat
选择选项 1
```

**Mac/Linux用户：**
```bash
chmod +x deploy.sh
./deploy.sh
选择选项 1
```

### 手动部署
```bash
# 1. 启动服务
docker-compose up -d

# 2. 查看日志
docker-compose logs -f

# 3. 访问系统
浏览器打开: http://localhost:8080

# 4. 停止服务
docker-compose down
```

---

## 🚂 Railway部署（最简单）

### 步骤1：准备代码
```bash
# Windows
deploy.bat
选择选项 2

# Mac/Linux
./deploy.sh
选择选项 2
```

### 步骤2：部署到Railway

1. 访问 [railway.app](https://railway.app)
2. 使用GitHub登录
3. 点击 **New Project**
4. 选择 **Deploy from GitHub repo**
5. 选择你的项目仓库
6. 点击 **Add variables** 添加环境变量：
   ```
   SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/tiku
   SPRING_DATASOURCE_USERNAME=root
   SPRING_DATASOURCE_PASSWORD=你的密码
   ```
7. 点击 **Add Service** → **Database** → **MySQL**
8. 等待部署完成（约5分钟）

### 步骤3：配置数据库

1. 进入MySQL服务
2. 点击 **Data** → **Query**
3. 粘贴 `src/main/resources/db/init.sql` 的内容
4. 执行SQL

✅ 完成！访问Railway提供的域名即可

---

## 🎨 Render部署

### 一键部署

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy)

### 手动部署

1. 访问 [render.com](https://render.com)
2. 点击 **New** → **Blueprint**
3. 连接GitHub仓库
4. Render会自动读取 `render.yaml` 配置
5. 点击 **Apply**
6. 等待部署完成

---

## ⚡ Zeabur部署（国内推荐）

### 步骤1：创建项目

1. 访问 [zeabur.com](https://zeabur.com)
2. 创建新项目
3. 添加服务 → GitHub仓库

### 步骤2：配置数据库

1. 添加服务 → MySQL
2. 记录数据库连接信息

### 步骤3：配置环境变量

```env
SPRING_DATASOURCE_URL=jdbc:mysql://[数据库地址]:3306/tiku
SPRING_DATASOURCE_USERNAME=[用户名]
SPRING_DATASOURCE_PASSWORD=[密码]
```

### 步骤4：部署

点击部署按钮，等待完成

---

## 🐳 Docker镜像导出（离线部署）

### 打包镜像
```bash
# Windows
deploy.bat
选择选项 4

# Mac/Linux
./deploy.sh
选择选项 4
```

### 导出镜像
```bash
docker save tiku-system:latest -o tiku-system.tar
```

### 在其他机器导入
```bash
docker load -i tiku-system.tar
docker run -p 8080:8080 tiku-system:latest
```

---

## 🔧 环境变量配置

### 必需变量
```env
# 数据库配置
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/tiku
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password

# JWT配置
JWT_SECRET=your_secret_key_here
JWT_EXPIRATION=86400000
```

### 可选变量
```env
# 服务器端口
SERVER_PORT=8080

# 日志级别
LOGGING_LEVEL_ROOT=INFO
```

---

## ❓ 常见问题

### Q1: Docker启动失败
**A:** 确保Docker Desktop正在运行

### Q2: 端口被占用
**A:** 修改 `docker-compose.yml` 中的端口映射
```yaml
ports:
  - "9090:8080"  # 改为9090端口
```

### Q3: 数据库连接失败
**A:** 检查环境变量配置是否正确

### Q4: Railway部署失败
**A:** 检查 `railway.json` 配置，确保Dockerfile路径正确

### Q5: 前端访问404
**A:** 确认前端已正确打包到Docker镜像中

---

## 📞 技术支持

遇到问题？
- 📧 提交Issue
- 💬 查看项目文档
- 🔍 搜索常见问题

---

## 🎉 部署成功后

1. 访问系统主页
2. 注册管理员账号
3. 导入题库数据（xlsx文件夹中）
4. 开始使用！

**默认管理员账号：**
- 用户名: admin
- 密码: admin123

⚠️ **重要：** 首次登录后请立即修改密码！

---

## 📊 性能优化建议

### Railway/Render免费版
- ✅ 适合个人学习测试
- ⚠️ 15分钟无访问会休眠
- 💡 建议：配置定时访问脚本保持活跃

### 生产环境
- 建议升级到付费套餐
- 或使用云服务器（阿里云/腾讯云学生机）

---

**祝你部署成功！🎊**

