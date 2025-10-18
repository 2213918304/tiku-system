# 🚀 快速启动指南

## 前置条件

- ✅ JDK 17+
- ✅ Maven 3.8+
- ✅ MySQL 8.0+
- ✅ Redis 6.0+（可选，暂未强制要求）

## 5分钟快速启动

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS tiku_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 配置数据库连接

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tiku_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: luo125314  # 修改为你的密码
```

### 3. 配置硅基流动API（AI判题）

在 `application.yml` 中配置：

```yaml
ai:
  siliconflow:
    api-key: YOUR_API_KEY_FROM_CLOUD_SILICONFLOW_CN  # 在 https://cloud.siliconflow.cn/account/ak 获取
```

> 📌 不配置API Key也能启动，但简答题无法使用AI判题功能

### 4. 启动应用

```bash
mvn spring-boot:run
```

应用将在 **http://localhost:8080/api** 启动

### 5. 查看API文档

访问 Swagger UI：**http://localhost:8080/api/swagger-ui.html**

### 6. 登录管理员账号

系统会自动创建默认管理员：

- **用户名**：admin
- **密码**：admin123

⚠️ **重要**：首次登录后请立即修改密码！

### 7. 导入题库数据

#### 方法1：使用API导入（推荐）

```bash
# 1. 登录获取token
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

# 2. 调用导入接口
POST http://localhost:8080/api/admin/data-import/mayuan
Authorization: Bearer YOUR_TOKEN
```

#### 方法2：使用Swagger UI

1. 访问 http://localhost:8080/api/swagger-ui.html
2. 找到"数据导入"标签
3. 点击"POST /api/admin/data-import/mayuan"
4. 点击"Try it out"
5. 点击"Execute"

### 8. 创建学生账号测试

```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "student001",
  "password": "123456",
  "email": "student@example.com",
  "realName": "张三"
}
```

### 9. 开始刷题！

登录学生账号后，可以体验9种刷题模式：

```bash
# 查看所有刷题模式
GET http://localhost:8080/api/practice/modes
Authorization: Bearer STUDENT_TOKEN

# 开始随机刷题
POST http://localhost:8080/api/practice/random
Authorization: Bearer STUDENT_TOKEN
Content-Type: application/json

{
  "subjectId": 1,
  "count": 20
}
```

## 🎯 核心功能测试

### 1. 题库管理（教师/管理员）

- 查看学科列表：`GET /api/subjects/enabled`
- 查看章节树：`GET /api/chapters/subject/{subjectId}/tree`
- 搜索题目：`POST /api/questions/search`

### 2. 9种刷题模式（学生）

| 模式 | 接口 | 说明 |
|-----|------|------|
| 顺序刷题 | POST /api/practice/sequential | 按顺序练习 |
| 随机刷题 | POST /api/practice/random | 随机抽题 |
| 章节练习 | POST /api/practice/chapter | 指定章节 |
| 考试模拟 | POST /api/practice/exam | 模拟考试 |
| 错题强化 | POST /api/practice/wrong-questions | 针对错题 |
| 收藏专练 | POST /api/practice/favorites | 复习收藏 |
| 闯关模式 | POST /api/practice/challenge | 游戏化 |
| 限时挑战 | POST /api/practice/timed | 限时答题 |
| 智能推荐 | POST /api/practice/smart-recommend | AI推荐 |

### 3. 答题和判题

```bash
# 提交单题答案
POST http://localhost:8080/api/grading/submit
Authorization: Bearer TOKEN
Content-Type: application/json

{
  "questionId": 1,
  "userAnswer": {
    "answer": "A"
  }
}
```

### 4. 收藏和笔记

```bash
# 收藏题目
POST http://localhost:8080/api/favorites?questionId=1&category=重点
Authorization: Bearer TOKEN

# 添加笔记
POST http://localhost:8080/api/notes
Authorization: Bearer TOKEN
Content-Type: application/json

{
  "questionId": 1,
  "title": "我的笔记",
  "content": "这道题的关键是..."
}
```

### 5. 错题本

```bash
# 查看错题
GET http://localhost:8080/api/wrong-questions
Authorization: Bearer TOKEN

# 统计错题数
GET http://localhost:8080/api/wrong-questions/count
Authorization: Bearer TOKEN
```

## 🔧 常见问题

### Q1: 启动失败，连接不上数据库

**检查**：
1. MySQL是否启动
2. 数据库密码是否正确
3. 数据库是否创建

### Q2: 导入题库失败

**检查**：
1. `马原题库4.0.html` 文件是否在项目根目录
2. 是否有管理员权限
3. 查看日志中的详细错误

### Q3: AI判题不工作

**原因**：未配置硅基流动API Key

**解决**：
1. 访问 https://cloud.siliconflow.cn/account/ak
2. 获取API Key
3. 配置到 `application.yml` 中

### Q4: Swagger UI 访问404

**检查**：URL是否正确，应该是：
```
http://localhost:8080/api/swagger-ui.html
```

## 📱 接下来做什么？

✅ 已完成后端开发：
- 用户认证系统
- 题库管理
- 9种刷题模式
- AI智能判题
- 收藏笔记错题本

🚧 待开发：
- [ ] 统计分析和排行榜
- [ ] Vue3前端项目

## 📚 更多文档

- [硅基流动配置指南](SILICONFLOW_CONFIG.md)
- [数据导入指南](DATA_IMPORT_GUIDE.md)
- [完整README](../README.md)

---

**开始体验吧！** 🎉



