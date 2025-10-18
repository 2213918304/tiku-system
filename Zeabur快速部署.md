# ⚡ Zeabur 5分钟快速部署

## 🎯 超快速部署（适合已经会用Git的）

### 1️⃣ 推送代码到GitHub
```bash
git init
git add .
git commit -m "部署到Zeabur"
git remote add origin https://github.com/你的用户名/tiku.git
git push -u origin main
```

### 2️⃣ 在Zeabur部署
```
1. 访问 https://zeabur.com
2. 登录（用GitHub账号）
3. 创建项目 → 添加Git服务 → 选择仓库
4. 添加MySQL数据库
5. 配置环境变量（见下方）
6. 生成域名
```

### 3️⃣ 环境变量（复制粘贴）
```bash
DATABASE_URL=jdbc:mysql://mysql.zeabur.internal:3306/tiku?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
DATABASE_USERNAME=root
DATABASE_PASSWORD=[从MySQL服务复制密码]
JWT_SECRET=your-super-secret-key-change-this
PORT=8080
```

### 4️⃣ 完成！
```
访问你的Zeabur域名
默认账号：admin / admin123
```

---

## 📝 新手详细版

看不懂上面的？没关系，查看 **Zeabur部署教程.md** 获取图文详解！

---

## 🆘 常见问题

### 没有GitHub账号？
```
1. 访问 https://github.com
2. 注册账号
3. 验证邮箱
```

### 不会用Git？
```
使用 GitHub Desktop：
1. 下载 https://desktop.github.com
2. 打开项目文件夹
3. Publish Repository
```

### 部署失败？
```
1. 查看日志找原因
2. 检查Dockerfile
3. 重新部署
```

### 费用担心？
```
新用户送$5，够用1-2周
之后约￥15-30/月
可以随时暂停服务
```

---

## 💡 提示

- 首次部署约5-8分钟
- 国内访问速度很快
- 支持支付宝/微信充值
- 中文界面，操作简单

---

**准备好了？开始部署！** 🚀

详细教程：**Zeabur部署教程.md**

