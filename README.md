# 🎓 智能题库刷题系统

一个基于 **Spring Boot 3 + Vue 3 + Element Plus** 开发的在线题库刷题系统，支持**多学科**、**13种题型**、**AI智能判题**，特别适合期末考试刷题复习。

## ✨ 核心特性

### 📚 多学科支持
- 支持马克思主义、毛概、近代史、思修等多个学科
- 每个学科独立的章节体系
- 灵活的学科扩展能力

### 📝 13种题型全支持
- **客观题**：单选题、多选题、判断题、填空题、排序题、匹配题
- **主观题**：简答题、论述题、案例分析题、材料分析题
- **其他**：计算题、组合题、编程题

### 🤖 AI智能判题
- 集成**硅基流动（SiliconCloud）**AI服务
- 主观题自动批改，生成详细评语
- 支持人工复核和评分调整
- 多维度评分（要点完整性、准确性、逻辑性等）

### 🎯 9种刷题模式
1. **顺序刷题** - 按题目顺序依次练习
2. **随机刷题** - 随机抽取题目
3. **章节专项练习** - 针对性突破薄弱章节
4. **考试模拟模式** - 模拟真实考试场景
5. **错题强化模式** - 专攻易错题
6. **收藏题专练** - 复习重点题目
7. **闯关模式** - 游戏化激励学习
8. **限时挑战** - 快速答题训练
9. **智能推荐** - AI推荐薄弱知识点

### 📊 数据统计分析
- 学习进度追踪
- 正确率统计（按章节、题型）
- 学习曲线可视化
- 错题分析报告
- 成就徽章系统

## 🛠️ 技术栈

### 后端
- **框架**：Spring Boot 3.5.6
- **数据库**：MySQL 8.0 + JPA
- **安全**：Spring Security + JWT
- **AI服务**：硅基流动 SiliconCloud
- **工具**：Lombok、Hutool、FastJSON2、EasyExcel

### 前端（待开发）
- **框架**：Vue 3 + Vite
- **UI组件**：Element Plus
- **状态管理**：Pinia
- **图表**：ECharts

## 📦 项目结构

```
TIKU/
├── src/main/java/com/springboot/tiku/
│   ├── common/              # 通用类（Result、ResultCode）
│   ├── config/              # 配置类（Security、JPA、CORS）
│   ├── controller/          # 控制器层
│   │   ├── AuthController       # 认证接口
│   │   ├── SubjectController    # 学科管理
│   │   ├── ChapterController    # 章节管理
│   │   ├── QuestionController   # 题目管理
│   │   └── GradingController    # 判题接口
│   ├── dto/                 # 数据传输对象
│   ├── entity/              # 实体类（16个核心实体）
│   ├── exception/           # 异常处理
│   ├── repository/          # 数据访问层
│   ├── security/            # 安全相关
│   ├── service/             # 业务逻辑层
│   │   ├── grading/            # 判题策略
│   │   │   ├── GradingStrategy     # 判题策略接口
│   │   │   ├── AutoGradingStrategy # 客观题自动判题
│   │   │   └── AIGradingStrategy   # AI判题（主观题）
│   │   └── ai/                 # AI服务
│   │       └── SiliconFlowAIService  # 硅基流动AI服务
│   └── utils/               # 工具类
├── src/main/resources/
│   ├── application.yml      # 主配置文件
│   └── db/                  # 数据库脚本
├── docs/                    # 文档目录
│   └── SILICONFLOW_CONFIG.md  # 硅基流动配置指南
└── pom.xml                  # Maven配置
```

## 🚀 快速开始

### 1. 环境要求
- JDK 17+
- MySQL 8.0+
- Maven 3.6+
- Redis 5.0+（可选）

### 2. 数据库配置

创建数据库：
```sql
CREATE DATABASE tiku_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改 `application.yml` 中的数据库配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tiku_db
    username: root
    password: your_password
```

### 3. 配置硅基流动AI（必须）

1. 访问 [SiliconCloud官网](https://cloud.siliconflow.cn/) 注册账号
2. 获取API Key：https://cloud.siliconflow.cn/account/ak
3. 修改 `application.yml` 中的AI配置：

```yaml
ai:
  siliconflow:
    api-key: YOUR_API_KEY_FROM_CLOUD_SILICONFLOW_CN  # 替换为你的API Key
    base-url: https://api.siliconflow.cn/v1
    model: Qwen/Qwen2.5-7B-Instruct
```

详细配置说明：[硅基流动配置指南](docs/SILICONFLOW_CONFIG.md)

### 4. 启动项目

```bash
# 编译项目
mvn clean install

# 启动应用
mvn spring-boot:run
```

应用将在 `http://localhost:8080/api` 启动

### 5. 启动前端项目

```bash
# 进入前端目录
cd tiku-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将在 `http://localhost:5173` 启动

### 6. 导入题库数据

**方式1：通过API导入（推荐）**

1. 登录管理员账号（admin/admin123）
2. 调用导入接口：
```bash
POST http://localhost:8080/api/admin/data-import/mayuan
Authorization: Bearer YOUR_TOKEN
```

**方式2：查看详细导入指南**

查看文档：[数据导入指南](docs/DATA_IMPORT_GUIDE.md)

### 7. 访问应用

**后端API文档**：http://localhost:8080/api/swagger-ui.html

**前端应用**：http://localhost:5173

**默认账号**：
- 管理员：admin / admin123
- 学生：student001 / 123456

### 8. 访问API文档

启动后访问 Swagger UI：
```
http://localhost:8080/api/swagger-ui.html
```

## 📖 API接口说明

### 认证接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### 学科管理
- `GET /api/subjects/enabled` - 获取所有启用的学科
- `POST /api/subjects` - 创建学科（管理员）
- `PUT /api/subjects/{id}` - 更新学科（管理员）

### 章节管理
- `GET /api/chapters/subject/{subjectId}/tree` - 获取学科章节树
- `POST /api/chapters` - 创建章节（教师/管理员）

### 题目管理
- `POST /api/questions/search` - 分页查询题目
- `GET /api/questions/random` - 随机获取题目
- `POST /api/questions` - 创建题目（教师/管理员）
- `PUT /api/questions/{id}` - 更新题目

### 判题接口
- `POST /api/grading/submit` - 提交答案并判题
- `POST /api/grading/submit/batch` - 批量提交答案（考试）

### 刷题模式接口
- `GET /api/practice/modes` - 获取所有刷题模式
- `POST /api/practice/start` - 开始刷题（通用接口）
- `POST /api/practice/random` - 随机刷题
- `POST /api/practice/chapter` - 章节练习
- `POST /api/practice/exam` - 考试模拟
- `POST /api/practice/wrong-questions` - 错题强化
- `POST /api/practice/favorites` - 收藏专练
- `POST /api/practice/challenge` - 闯关模式
- `POST /api/practice/timed` - 限时挑战
- `POST /api/practice/smart-recommend` - 智能推荐

### 收藏和笔记接口
- `POST /api/favorites` - 收藏题目
- `DELETE /api/favorites/{questionId}` - 取消收藏
- `GET /api/favorites` - 获取我的收藏列表
- `POST /api/notes` - 添加笔记
- `PUT /api/notes/{id}` - 更新笔记
- `DELETE /api/notes/{id}` - 删除笔记

### 错题本接口
- `GET /api/wrong-questions` - 获取我的错题列表
- `GET /api/wrong-questions/count` - 统计错题数
- `DELETE /api/wrong-questions/{questionId}` - 移除错题

### 数据导入接口（管理员）
- `POST /api/admin/data-import/mayuan` - 导入马原题库

### 统计分析接口
- `GET /api/statistics/my` - 获取我的学习统计
- `GET /api/statistics/subjects` - 获取学科学习统计
- `GET /api/statistics/chapters/{subjectId}` - 获取章节学习统计
- `GET /api/statistics/calendar` - 获取学习日历
- `POST /api/statistics/check-in` - 每日打卡

### 排行榜接口
- `GET /api/ranking/answer-count` - 答题数排行榜
- `GET /api/ranking/accuracy` - 正确率排行榜
- `GET /api/ranking/points` - 积分排行榜
- `GET /api/ranking/subject/{subjectId}` - 学科排行榜

## 🎯 判题系统说明

### 客观题自动判题
支持题型：单选题、多选题、判断题、填空题、排序题、匹配题

**特点**：
- 即时判题，秒级响应
- 支持多种答案格式
- 填空题支持同义词匹配

### 主观题AI判题
支持题型：简答题、论述题、案例分析题、材料分析题

**特点**：
- 基于大语言模型智能评分
- 多维度评分（要点完整性、准确性、逻辑性等）
- 生成详细的评语和改进建议
- 低置信度自动转人工复核

**判题流程**：
```
学生提交答案 → 判题策略选择 → AI模型评分 → 解析结果 → 
→ 置信度判断 → 保存记录 → 更新统计 → 处理错题本
```

## 🔐 权限说明

### 角色类型
- **STUDENT**（学生）：答题、查看统计、收藏、笔记
- **TEACHER**（教师）：+ 创建/编辑题目、管理章节
- **ADMIN**（管理员）：+ 所有权限（管理学科、用户、系统配置）

### 默认管理员账号
```
用户名：admin
密码：admin123
```

首次登录后请立即修改密码！

## 📊 数据库设计

### 核心表
- `user` - 用户表
- `subject` - 学科表
- `chapter` - 章节表
- `question` - 题目表（支持JSON存储多种题型）
- `answer_record` - 答题记录表
- `ai_grading_record` - AI判题记录表
- `wrong_question` - 错题本表
- `favorite` - 收藏表
- `note` - 笔记表
- `exam` - 考试表
- `exam_record` - 考试记录表

## 📝 开发进度

### ✅ 全部完成！
- [x] Phase 1: 项目配置和依赖管理
- [x] Phase 2: 数据库实体类设计（16个实体）
- [x] Phase 3: Repository数据访问层（13个Repository）
- [x] Phase 4: 统一响应和异常处理
- [x] Phase 5: JWT认证和Spring Security配置
- [x] Phase 6: 学科和章节管理功能
- [x] Phase 7: 题库管理功能（支持13种题型）
- [x] Phase 8: 判题系统（客观题自动判题+AI智能判题）
- [x] Phase 9: 9种刷题模式实现
- [x] Phase 10: 统计分析和排行榜功能
- [x] Phase 11: Vue3前端项目开发（现代化UI）
- [x] Phase 12: 数据初始化（导入马原题库）

## 🎊 项目已完成！

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 开发规范
- 代码风格：遵循阿里巴巴Java开发手册
- 提交信息：使用语义化的commit message
- 测试：确保新功能有对应的单元测试

## 📄 开源协议

本项目采用 MIT 协议开源

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 [GitHub Issue](https://github.com/your-repo/issues)
- 发送邮件至：your-email@example.com

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [硅基流动 SiliconCloud](https://cloud.siliconflow.cn/)
- [Element Plus](https://element-plus.org/)
- 所有贡献者

---

**⭐ 如果这个项目对你有帮助，请给个Star支持一下！**

