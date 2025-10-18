# ⚡ 5分钟快速部署指南

## 🎯 选择最适合你的方案

### 方案对比

| 平台 | 难度 | 速度 | 免费额度 | 国内访问 | 推荐度 |
|------|------|------|----------|----------|--------|
| **本地Docker** | ⭐ | ⚡⚡⚡ | 无限 | ✅ | ⭐⭐⭐⭐⭐ |
| **Railway** | ⭐⭐ | ⚡⚡ | $5/月 | ❌ | ⭐⭐⭐⭐ |
| **Zeabur** | ⭐⭐ | ⚡⚡ | 有限 | ✅ | ⭐⭐⭐⭐⭐ |
| **Render** | ⭐⭐⭐ | ⚡ | 有限 | ❌ | ⭐⭐⭐ |

---

## 🚀 方案1：本地Docker（最快）

### Windows用户
```bash
1. 双击 deploy.bat
2. 输入 1
3. 等待5分钟
4. 访问 http://localhost:8080
```

### Mac/Linux用户
```bash
chmod +x deploy.sh
./deploy.sh
# 选择 1
```

✅ **完成！** 系统已在本地运行

---

## 🌐 方案2：Railway（最简单的云部署）

### 准备（1分钟）
```bash
# 确保项目已推送到GitHub
git init
git add .
git commit -m "Ready for deployment"
git remote add origin https://github.com/你的用户名/tiku.git
git push -u origin main
```

### 部署（3分钟）

1. **打开Railway**  
   访问 [railway.app](https://railway.app)

2. **一键部署**  
   - 点击 `New Project`
   - 选择 `Deploy from GitHub repo`
   - 选择你的仓库

3. **添加数据库**  
   - 点击 `+ New`
   - 选择 `Database`
   - 选择 `Add MySQL`

4. **完成！**  
   - Railway自动构建和部署
   - 获取访问链接

💡 **提示：** Railway每月提供$5免费额度，足够个人使用

---

## 🇨🇳 方案3：Zeabur（国内最佳）

### 部署步骤（5分钟）

1. **注册登录**  
   访问 [zeabur.com](https://zeabur.com)

2. **创建项目**  
   - 点击 `创建项目`
   - 添加服务 → GitHub
   - 选择仓库

3. **添加数据库**  
   - 点击 `添加服务`
   - 选择 `MySQL`

4. **配置环境变量**  
   ```
   SPRING_DATASOURCE_URL=从数据库页面复制
   SPRING_DATASOURCE_USERNAME=从数据库页面复制
   SPRING_DATASOURCE_PASSWORD=从数据库页面复制
   ```

5. **部署！**

✅ **优势：** 国内访问速度快，中文界面

---

## 📦 一键部署按钮

### Railway
[![Deploy on Railway](https://railway.app/button.svg)](https://railway.app/new/template?template=https://github.com/你的用户名/tiku)

### Render
[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/你的用户名/tiku)

---

## 🎬 视频教程

📹 [观看部署视频教程](视频链接)

---

## 🆘 遇到问题？

### 常见错误速查

#### ❌ Docker启动失败
```bash
# 解决方案：检查Docker是否运行
docker ps

# 如果失败，重启Docker Desktop
```

#### ❌ 端口被占用
```bash
# 解决方案：修改端口
# 编辑 docker-compose.yml
ports:
  - "9090:8080"  # 改为其他端口
```

#### ❌ 数据库连接失败
```bash
# 解决方案：等待数据库完全启动
docker-compose logs mysql

# 重启服务
docker-compose restart app
```

---

## ✅ 部署成功检查清单

- [ ] 能访问主页
- [ ] 能注册账号
- [ ] 能登录系统
- [ ] 能看到题库列表
- [ ] 能答题

全部打勾？🎉 **恭喜部署成功！**

---

## 📚 下一步

1. **导入题库数据**  
   - 进入管理后台
   - 上传 `xlsx/辽师大法题库_101-300题.xlsx`

2. **创建管理员**  
   - 注册账号
   - 在数据库中设置角色为admin

3. **开始使用**  
   - 浏览题库
   - 开始练习
   - 查看统计

---

## 🎯 性能优化

### Railway/Render防休眠
```bash
# 使用UptimeRobot免费监控
# 每5分钟ping一次，保持活跃
https://uptimerobot.com
```

### 加速访问
```bash
# 使用CDN加速静态资源
# 推荐：Cloudflare（免费）
```

---

## 💡 专业提示

1. **安全性**
   - 修改默认密码
   - 设置JWT密钥
   - 启用HTTPS

2. **备份**
   - 定期导出数据库
   - 保存代码到GitHub

3. **监控**
   - 查看运行日志
   - 监控服务状态

---

**准备好了吗？选择一个方案，开始部署！** 🚀

