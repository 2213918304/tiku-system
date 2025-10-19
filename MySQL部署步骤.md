# 在Zeabur部署MySQL的详细步骤

## 1. 添加MySQL服务

```
1. 在Zeabur项目页面
2. 点击 "Add Service" 按钮
3. 选择 "Marketplace"（服务市场）
4. 搜索 "MySQL"
5. 选择 "MySQL 8.0"
6. 点击 "Deploy" 部署
7. 等待部署完成（1-2分钟）
```

## 2. 查看MySQL连接信息

**方式A：通过Variables查看**
```
1. 点击MySQL服务卡片
2. 点击 "Variables" 标签
3. 查看以下变量：
   - MYSQL_HOST（主机地址）
   - MYSQL_PORT（端口，通常是3306）
   - MYSQL_DATABASE（数据库名，通常是zeabur）
   - MYSQL_USERNAME（用户名，通常是root）
   - MYSQL_PASSWORD（密码，自动生成的）
```

**方式B：通过Instructions查看**
```
1. 点击MySQL服务卡片
2. 点击 "Instructions" 或 "Connection" 标签
3. 复制连接信息
```

## 3. 配置应用环境变量

在你的应用服务中添加这些变量：

```bash
# 方法1：使用Zeabur自动注入的变量（推荐）
SPRING_DATASOURCE_URL=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=${MYSQL_USERNAME}
SPRING_DATASOURCE_PASSWORD=${MYSQL_PASSWORD}
JWT_SECRET=tiku-jwt-secret-2024-production
SPRING_PROFILES_ACTIVE=prod

# 方法2：手动填写（如果方法1不行）
SPRING_DATASOURCE_URL=jdbc:mysql://你复制的host:3306/zeabur?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=你复制的密码
JWT_SECRET=tiku-jwt-secret-2024-production
SPRING_PROFILES_ACTIVE=prod
```

## 4. 保存并重启

```
1. 点击 "Save" 保存环境变量
2. 应用会自动重启
3. 等待1-2分钟查看日志
```

