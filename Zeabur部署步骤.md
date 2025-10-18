# 🚀 Zeabur 部署教程（共享集群）

## 📋 前置准备

✅ 代码已推送到GitHub：https://github.com/2213918304/tiku-system
✅ 已有Zeabur账号

---

## 🎯 部署步骤

### 第一步：创建项目

1. 访问 **Zeabur控制台**：https://zeabur.com/dashboard
2. 点击 **"Create Project"** 或 **"新建项目"**
3. 选择 **"共享集群 (Shared Cluster)"** ⭐ 免费选项
4. 项目名称：`tiku-system`

---

### 第二步：添加服务

#### 1️⃣ 添加数据库（MySQL）

```
1. 点击 "Add Service" → "Marketplace"
2. 搜索并选择 "MySQL 8.0"
3. 点击 "Deploy"
4. 等待部署完成
5. 点击MySQL服务 → 复制连接信息：
   - Host
   - Port
   - Database
   - Username
   - Password
```

#### 2️⃣ 添加Redis（可选）

```
1. 点击 "Add Service" → "Marketplace"
2. 搜索并选择 "Redis"
3. 点击 "Deploy"
4. 等待部署完成
```

#### 3️⃣ 添加应用服务（Spring Boot）

```
1. 点击 "Add Service" → "Git"
2. 选择你的GitHub账号
3. 选择仓库：2213918304/tiku-system
4. 选择分支：main
5. 点击 "Deploy"
```

---

### 第三步：配置环境变量

点击应用服务 → **"Variables"** 标签页，添加：

```bash
# 数据库配置（从MySQL服务复制）
SPRING_DATASOURCE_URL=jdbc:mysql://<MYSQL_HOST>:<PORT>/tiku?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
SPRING_DATASOURCE_USERNAME=<从MySQL服务复制>
SPRING_DATASOURCE_PASSWORD=<从MySQL服务复制>

# Redis配置（如果部署了Redis）
SPRING_REDIS_HOST=<REDIS_HOST>
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=<从Redis服务复制>

# AI配置（可选）
SILICONFLOW_API_KEY=你的API密钥

# JWT密钥
JWT_SECRET=your-256-bit-secret-key-change-this-in-production

# 激活生产环境配置
SPRING_PROFILES_ACTIVE=prod
```

---

### 第四步：配置域名（可选）

```
1. 点击应用服务 → "Networking" 标签页
2. 点击 "Generate Domain" 生成免费域名
3. 或者绑定自己的域名
```

---

### 第五步：查看部署状态

```
1. 点击应用服务 → "Logs" 查看日志
2. 等待构建和部署完成
3. 看到 "Started TikuApplication" 表示启动成功
```

---

## ✅ 部署完成

访问生成的域名，例如：
- https://tiku-system.zeabur.app

---

## 🔧 常见问题

### 1. 构建失败？
- 检查 `pom.xml` 是否正确
- 检查 `Dockerfile` 是否存在
- 查看构建日志定位错误

### 2. 启动失败？
- 检查环境变量是否配置正确
- 检查数据库连接是否正常
- 查看应用日志

### 3. 数据库连接失败？
- 确保MySQL服务已启动
- 确保环境变量中的数据库信息正确
- 检查数据库名称、用户名、密码

---

## 📊 监控和管理

- **查看日志**：Logs 标签页
- **资源使用**：Metrics 标签页
- **重启服务**：右上角菜单 → Restart
- **暂停服务**：右上角菜单 → Suspend

---

## 💰 费用说明

**共享集群（免费）：**
- ✅ 每月 $5 免费额度
- ✅ 适合小型项目
- ⚠️ 资源有限，性能较低

**如需更多资源，可升级到专属集群。**

---

## 🎉 祝部署成功！

如有问题，请查看：
- Zeabur文档：https://zeabur.com/docs
- GitHub仓库：https://github.com/2213918304/tiku-system

