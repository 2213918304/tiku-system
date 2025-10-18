# 📚 数据导入指南

## 概述

本系统支持从HTML文件导入题库数据。当前已实现马克思主义基本原理题库的导入功能。

## 🚀 快速开始

### 方式1：通过API手动导入（推荐）

1. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

2. **登录管理员账号**
   - 默认用户名：`admin`
   - 默认密码：`admin123`
   - 登录接口：`POST /api/auth/login`

3. **调用导入接口**
   ```bash
   POST /api/admin/data-import/mayuan
   Authorization: Bearer YOUR_TOKEN
   ```

4. **查看导入结果**
   ```json
   {
     "code": 200,
     "message": "导入成功",
     "data": {
       "subject": "马克思主义基本原理",
       "totalQuestions": 100,
       "chapters": 8
     }
   }
   ```

### 方式2：自动导入（首次启动）

在 `DataInitRunner.java` 中启用自动导入：

```java
@Override
public void run(String... args) {
    // 1. 创建管理员账号
    createAdminUser();
    
    // 2. 自动导入题库（取消注释）
    checkAndImportQuestions();  // ✅ 启用这行
}
```

## 📖 数据格式说明

### HTML文件格式

HTML文件中的题目数据以JavaScript数组格式存储：

```javascript
const questions = [
    {
        id: 1,
        type: "single",        // 题型：single-单选, short-简答
        unit: "导论",           // 章节名称
        question: "题目内容",
        options: ["A.选项1", "B.选项2", "C.选项3", "D.选项4"],
        answer: "A",           // 答案
        explanation: "答案解析"
    },
    {
        id: 2,
        type: "short",
        unit: "第一章",
        question: "简述题目内容",
        options: [],
        answer: "参考答案（分值5）",  // 可包含分值标记
        explanation: "解析内容"
    }
];
```

### 支持的题型

| HTML中的type | 系统中的题型 | 说明 |
|--------------|-------------|------|
| `single` | SINGLE | 单选题 |
| `short` | SHORT_ANSWER | 简答题（自动启用AI判题） |

### 数据转换规则

1. **章节创建**
   - 自动根据 `unit` 字段创建章节
   - 章节名称去重
   - 按出现顺序排序

2. **分值识别**
   - 从答案中提取：`（分值5）` → 5分
   - 单选题默认：2分
   - 简答题默认：5分

3. **难度设置**
   - 默认为中等难度（MEDIUM）
   - 可后期通过API调整

4. **AI判题配置**
   - 简答题自动启用AI判题
   - 单选题使用自动判题

## 🔄 导入流程

```
读取HTML文件
    ↓
提取questions数组
    ↓
JavaScript → JSON转换
    ↓
解析题目数据
    ↓
创建/获取学科（马克思主义基本原理）
    ↓
创建章节（导论、第一章、第二章...）
    ↓
逐题导入数据库
    ↓
更新统计信息
    ↓
完成导入
```

## 📊 导入后数据结构

### 学科（Subject）
```
名称：马克思主义基本原理
代码：mayuan
状态：启用
题目数：根据实际导入数量
```

### 章节（Chapter）
```
导论
第一章：世界的物质性及发展规律
第二章：实践与认识及其发展规律
第三章：人类社会及其发展规律
... (根据HTML文件中的unit字段)
```

### 题目（Question）
- 单选题：自动判题
- 简答题：AI判题（需配置硅基流动API）

## ⚙️ 配置项

### application.yml

```yaml
app:
  admin:
    username: admin       # 管理员用户名
    password: admin123    # 管理员密码（首次启动后请修改）
    email: admin@tiku.com # 管理员邮箱
```

## 🔍 验证导入

### 1. 检查学科
```bash
GET /api/subjects/enabled
```

### 2. 检查章节
```bash
GET /api/chapters/subject/{subjectId}/tree
```

### 3. 检查题目
```bash
POST /api/questions/search
{
  "subjectId": 1,
  "status": 1
}
```

### 4. 查看统计
- 题目总数
- 按章节统计
- 按题型统计

## ⚠️ 注意事项

### 1. 文件路径
确保 `马原题库4.0.html` 文件在项目根目录下：
```
TIKU/
├── 马原题库4.0.html  ✅ 放在这里
├── src/
├── pom.xml
└── ...
```

### 2. 数据库
- 确保数据库连接正常
- 表结构会自动创建（JPA的 ddl-auto=update）
- 建议首次导入前备份数据库

### 3. 重复导入
- 目前不会自动去重
- 重复导入会创建新的题目记录
- 建议：导入前检查是否已有数据

### 4. 性能
- 100题约需1-2秒
- 1000题约需10-20秒
- 大批量导入建议分批进行

## 🐛 常见问题

### Q1: 导入失败，提示"未找到题目数据"

**原因**：
- HTML文件路径不正确
- HTML文件格式不匹配

**解决**：
1. 确认文件路径：`马原题库4.0.html`
2. 检查文件中是否包含 `const questions = [...]`

### Q2: 导入成功，但题目数为0

**原因**：
- JavaScript到JSON转换失败
- 数据格式不符合预期

**解决**：
1. 查看日志中的错误信息
2. 检查HTML文件中的JavaScript语法

### Q3: 部分题目导入失败

**原因**：
- 某些题目数据格式异常
- 必填字段缺失

**解决**：
1. 查看日志找到失败的题目
2. 修复数据后重新导入该题目

### Q4: 章节没有正确创建

**原因**：
- unit字段为空或格式不一致

**解决**：
1. 统一章节命名格式
2. 确保每个题目都有unit字段

## 📈 扩展导入功能

### 支持更多学科

1. 创建新的导入方法：
```java
public Map<String, Object> importOtherSubject(String htmlFilePath) {
    // 类似逻辑
}
```

2. 添加API接口：
```java
@PostMapping("/other-subject")
public Result<Map<String, Object>> importOtherSubject() {
    // ...
}
```

### 支持Excel导入

使用EasyExcel读取Excel文件：
```java
EasyExcel.read(file, QuestionExcelDTO.class, new ExcelListener())
    .sheet()
    .doRead();
```

### 支持JSON导入

直接解析JSON格式的题库文件：
```java
JSONArray questions = JSON.parseArray(jsonContent);
```

## 📞 技术支持

导入过程中遇到问题？

1. 查看日志文件：`logs/tiku.log`
2. 检查数据库表：`question`, `subject`, `chapter`
3. 联系技术支持

---

**祝您导入顺利！** 🎉

