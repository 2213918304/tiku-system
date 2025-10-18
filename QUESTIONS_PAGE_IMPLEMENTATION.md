# 题目管理页面前后端对接完成说明

## 完成时间
2025-10-17

## 主要修改内容

### 1. 前端 API 更新 (`tiku-frontend/src/api/modules/question.ts`)

添加了完整的题目管理API方法：
- `list()` - 分页查询题目列表（使用POST /questions/search）
- `create()` - 创建题目
- `update()` - 更新题目
- `delete()` - 删除题目
- `batchDelete()` - 批量删除题目

### 2. 类型定义更新 (`tiku-frontend/src/types/index.ts`)

更新了 `Question` 接口，使其与后端DTO完全匹配：
- 添加 `content` 字段（题目内容）
- 更新 `options` 为 any 类型（支持多种格式）
- 更新 `answer` 为 any 类型（支持JSON对象）
- 添加 AI 相关字段（`aiGradingEnabled`, `aiGradingConfig` 等）
- 添加统计字段（`useCount`, `correctCount`, `wrongCount`）

### 3. Questions.vue 页面重大更新

#### 3.1 题型枚举对齐
**前端旧值** → **后端值**：
- `SINGLE_CHOICE` → `SINGLE`（单选题）
- `MULTIPLE_CHOICE` → `MULTIPLE`（多选题）
- `TRUE_FALSE` → `JUDGE`（判断题）
- `FILL_BLANK` → `FILL`（填空题）
- `SHORT_ANSWER` → `SHORT_ANSWER`（简答题）

新增支持：
- `ESSAY` - 论述题
- `CASE_ANALYSIS` - 案例分析
- `MATERIAL_ANALYSIS` - 材料分析
- `CALCULATION` - 计算题
- `ORDERING` - 排序题
- `MATCHING` - 匹配题
- `COMPOSITE` - 组合题
- `PROGRAMMING` - 编程题

#### 3.2 难度显示方式修改
- **旧方式**：使用 `el-rate` 组件显示1-5星级
- **新方式**：使用 `el-tag` 显示文字（简单/中等/困难）
- 后端枚举：`EASY`、`MEDIUM`、`HARD`

#### 3.3 字段名对齐
**前端旧字段** → **后端字段**：
- 列表显示：`content` → `title`（题目内容）
- 表单字段：`content` → `title`（题目标题）
- 答案字段：`correctAnswer` → `answer`
- 解析字段：`explanation` → `answerAnalysis`

#### 3.4 答案格式处理
后端要求 `answer` 字段为 JSON 对象格式：
- **单选题**：`{ "answer": "A" }`
- **多选题**：`{ "answer": ["A", "B", "C"] }`
- **判断题**：`{ "answer": true }`
- **其他题型**：`{ "answer": "答案内容" }`

前端在提交时自动转换：
```javascript
if (formData.type === 'SINGLE') {
  data.answer = { answer: formData.answer }
} else if (formData.type === 'MULTIPLE') {
  const answerArray = formData.answer.split('').filter(c => c.trim())
  data.answer = { answer: answerArray }
} else if (formData.type === 'JUDGE') {
  data.answer = { answer: formData.answer === 'true' || formData.answer === true }
} else {
  data.answer = { answer: formData.answer }
}
```

在查看/编辑时自动解析：
```javascript
let answer = ''
if (row.answer && typeof row.answer === 'object') {
  const answerObj = row.answer as any
  if (Array.isArray(answerObj.answer)) {
    answer = answerObj.answer.join('')
  } else {
    answer = String(answerObj.answer || '')
  }
}
```

#### 3.5 分页响应处理
后端返回 Spring Data 的 `Page` 格式，前端正确提取数据：
```javascript
if (res.data) {
  questionList.value = res.data.content || []
  pagination.total = res.data.totalElements || 0
}
```

### 4. 新增辅助方法

添加了难度相关的辅助方法：
- `getDifficultyColor()` - 获取难度标签颜色
- `getDifficultyName()` - 获取难度中文名称

更新了题型相关方法，支持所有13种题型。

## 功能列表

### ✅ 已完成功能

1. **题目列表展示**
   - 分页加载
   - 题型、难度标签显示
   - 学科、章节信息显示

2. **搜索筛选**
   - 按学科筛选（支持联动章节）
   - 按章节筛选
   - 按题型筛选
   - 按关键词搜索

3. **题目管理**
   - 新增题目
   - 编辑题目
   - 删除题目
   - 查看题目详情

4. **表单验证**
   - 学科、章节必填
   - 题目内容必填
   - 答案必填

5. **动态表单**
   - 根据题型动态显示选项
   - 单选/多选题支持动态添加/删除选项
   - 不同题型显示不同的答案提示

## 后端接口说明

### 主要接口

1. **POST /questions/search** - 分页查询题目
   - 参数：page, size（作为 query params）
   - 请求体：`QuestionQueryRequest`（包含筛选条件）
   - 返回：`Page<QuestionDTO>`

2. **POST /questions** - 创建题目
   - 请求体：`QuestionRequest`
   - 返回：`QuestionDTO`

3. **PUT /questions/{id}** - 更新题目
   - 路径参数：题目ID
   - 请求体：`QuestionRequest`
   - 返回：`QuestionDTO`

4. **DELETE /questions/{id}** - 删除题目
   - 路径参数：题目ID
   - 返回：void

5. **GET /questions/{id}** - 获取题目详情
   - 路径参数：题目ID
   - 返回：`QuestionDTO`

## 使用说明

1. 访问 `http://localhost:5173/admin/questions`
2. 页面会自动加载学科列表和题目列表
3. 可以通过搜索条件筛选题目
4. 点击"新增题目"按钮创建新题目
5. 点击"编辑"按钮修改题目
6. 点击"查看"按钮查看题目详情（只读模式）
7. 点击"删除"按钮删除题目

## 注意事项

1. **答案格式**：前端用户输入的是简单字符串（如"A"、"AB"），但提交时会自动转换为后端需要的JSON对象格式

2. **选项格式**：后端存储的是字符串数组，前端在编辑时需要转换为 `{ content: string }[]` 格式

3. **难度枚举**：必须使用 `EASY`、`MEDIUM`、`HARD`，不能使用数字

4. **题型枚举**：必须与后端 `Question.QuestionType` 枚举完全一致

5. **分页**：前端页码从1开始，后端从0开始，需要 `-1` 转换

## 测试建议

1. 测试各种题型的创建和编辑
2. 测试选项的动态添加和删除
3. 测试搜索筛选功能
4. 测试分页功能
5. 测试表单验证
6. 测试删除功能

## 后续优化建议

1. 添加批量删除功能
2. 添加题目导入/导出功能
3. 添加富文本编辑器支持
4. 添加图片上传功能
5. 添加题目预览功能
6. 添加题目统计信息显示






