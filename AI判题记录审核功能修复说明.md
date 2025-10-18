# AI判题记录审核功能修复说明

## 🎯 问题描述

**原问题：** 管理后台的"AI判题审核"页面看不到任何记录

### 具体情况
- 学生答主观题后，AI成功判题
- 但管理员在"答题记录管理"能看到记录
- 在"AI判题审核"页面却看不到
- **原因**：AI判题后没有保存到 `ai_grading_record` 表

---

## ✅ 解决方案

### 核心修改

在 `GradingService` 中，AI判题成功后自动保存到 `ai_grading_record` 表。

### 数据流程

```
用户提交主观题答案
    ↓
AI判题（AIGradingStrategy）
    ↓
保存答题记录（answer_record表）
    ↓
【新增】保存AI判题记录（ai_grading_record表） ✨
    ↓
更新统计数据
```

---

## 🔧 技术实现

### 1. 修改 `GradingService.java`

#### （1）注入 `AiGradingRecordRepository`

```java
private final AiGradingRecordRepository aiGradingRecordRepository;
```

#### （2）在判题后保存AI记录

```java
// 保存答题记录
AnswerRecord record = saveAnswerRecord(request, userId, question, result);
result.setAnswerRecordId(record.getId());

// 如果是AI判题，保存AI判题记录
if ("AI".equals(result.getGradingType()) && result.getAiFeedback() != null) {
    saveAIGradingRecord(record, question, result);
}
```

#### （3）新增保存AI判题记录的方法

```java
/**
 * 保存AI判题记录
 */
private void saveAIGradingRecord(AnswerRecord answerRecord, Question question, GradingResult result) {
    try {
        AiGradingRecord aiRecord = new AiGradingRecord();
        aiRecord.setAnswerRecordId(answerRecord.getId());
        aiRecord.setQuestionId(question.getId());
        aiRecord.setUserId(answerRecord.getUserId());
        
        // 提取用户答案文本
        String userAnswerText = extractAnswerText(answerRecord.getUserAnswer());
        aiRecord.setStudentAnswer(userAnswerText);
        
        // 设置AI判题结果
        aiRecord.setAiScore(result.getScore());
        aiRecord.setAiConfidence(result.getAiFeedback().getConfidence());
        aiRecord.setAiModel(result.getAiFeedback().getModel());
        
        // 序列化AI反馈为JSON
        String aiFeedbackJson = objectMapper.writeValueAsString(result.getAiFeedback());
        aiRecord.setAiFeedback(aiFeedbackJson);
        
        // 设置是否需要人工复核
        aiRecord.setManualReview(result.getNeedManualReview() != null 
            && result.getNeedManualReview());
        
        // 如果不需要人工复核，最终分数就是AI分数
        if (!aiRecord.getManualReview()) {
            aiRecord.setFinalScore(result.getScore());
        }
        
        aiGradingRecordRepository.save(aiRecord);
        log.info("AI判题记录已保存：answerRecordId={}, questionId={}", 
            answerRecord.getId(), question.getId());
        
    } catch (Exception e) {
        log.error("保存AI判题记录失败：answerRecordId={}, error={}", 
            answerRecord.getId(), e.getMessage(), e);
        // 不抛出异常，避免影响主流程
    }
}
```

#### （4）新增提取答案文本的辅助方法

```java
/**
 * 提取答案文本
 */
private String extractAnswerText(String userAnswerJson) {
    try {
        // 尝试从JSON中提取answer字段
        com.alibaba.fastjson2.JSONObject json = 
            com.alibaba.fastjson2.JSON.parseObject(userAnswerJson);
        if (json != null && json.containsKey("answer")) {
            Object answer = json.get("answer");
            return answer != null ? answer.toString() : userAnswerJson;
        }
        return userAnswerJson;
    } catch (Exception e) {
        // 如果不是JSON，直接返回原文本
        return userAnswerJson;
    }
}
```

---

## 📊 数据库表结构

### `ai_grading_record` 表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `answer_record_id` | BIGINT | 答题记录ID |
| `question_id` | BIGINT | 题目ID |
| `user_id` | BIGINT | 用户ID |
| `student_answer` | TEXT | 学生答案 |
| `ai_model` | VARCHAR(50) | AI模型名称 |
| `ai_score` | DECIMAL(5,2) | AI评分 |
| `ai_confidence` | DECIMAL(3,2) | 置信度(0-1) |
| `ai_feedback` | JSON | AI反馈详情 |
| `manual_review` | BOOLEAN | 是否需要人工复核 |
| `manual_score` | DECIMAL(5,2) | 人工评分 |
| `reviewer_id` | BIGINT | 复核教师ID |
| `review_comment` | TEXT | 复核意见 |
| `final_score` | DECIMAL(5,2) | 最终得分 |
| `grading_time` | INT | 判题耗时(毫秒) |
| `created_at` | DATETIME | 创建时间 |
| `updated_at` | DATETIME | 更新时间 |

### AI反馈 JSON 结构

```json
{
  "model": "SiliconFlow",
  "confidence": 0.9,
  "scoreDetails": [
    {
      "dimension": "要点完整性",
      "score": 1.5,
      "maxScore": 2.0,
      "reason": "答案覆盖了基本原理..."
    }
  ],
  "strengths": [
    "准确复述了基本原理",
    "实例分析具体"
  ],
  "weaknesses": [
    "分析深度不足",
    "部分表述重复"
  ],
  "suggestions": "建议加强...",
  "comment": "本文较好地完成了题目要求..."
}
```

---

## 🖥️ 前端展示

### 管理后台 - AI判题审核页面

**路径：** `/admin/ai-grading`

**功能：**

1. **统计卡片**
   - 总判题数
   - 待审核数（低置信度）
   - 高置信度数
   - 平均置信度

2. **记录列表**
   - 题目预览
   - 用户答案预览
   - AI评分
   - 置信度（进度条显示）
   - 状态标签
   - 操作按钮（查看详情、人工复核）

3. **筛选功能**
   - 筛选状态：全部 / 待审核（低置信度）
   - 刷新按钮

4. **详情对话框**
   - 完整题目内容
   - 学生答案
   - AI评分详情（分项得分、优缺点、建议）
   - 置信度

5. **人工复核对话框**
   - 原AI评分
   - 调整分数
   - 审核备注

---

## 📝 保存逻辑详解

### 1. 触发条件

```java
if ("AI".equals(result.getGradingType()) && result.getAiFeedback() != null) {
    saveAIGradingRecord(record, question, result);
}
```

**条件：**
- 判题类型是 `AI`
- AI反馈不为空

**适用题型：**
- 论述题（ESSAY）
- 简答题（SHORT_ANSWER）
- 案例分析（CASE_ANALYSIS）
- 材料分析（MATERIAL_ANALYSIS）

### 2. 数据映射

| 来源 | 字段 | 目标 |
|------|------|------|
| `AnswerRecord` | `id` | `answer_record_id` |
| `Question` | `id` | `question_id` |
| `AnswerRecord` | `userId` | `user_id` |
| `AnswerRecord` | `userAnswer` | `student_answer` |
| `GradingResult.aiFeedback` | `model` | `ai_model` |
| `GradingResult` | `score` | `ai_score` |
| `GradingResult.aiFeedback` | `confidence` | `ai_confidence` |
| `GradingResult` | `aiFeedback` | `ai_feedback`(JSON) |
| `GradingResult` | `needManualReview` | `manual_review` |
| `GradingResult` | `score` | `final_score`(如果不需要复核) |

### 3. 错误处理

```java
catch (Exception e) {
    log.error("保存AI判题记录失败：answerRecordId={}, error={}", 
        answerRecord.getId(), e.getMessage(), e);
    // 不抛出异常，避免影响主流程
}
```

**设计理念：**
- AI判题记录保存失败不影响答题记录
- 记录错误日志便于排查
- 保证用户答题流程不被打断

---

## 🔍 日志示例

### 成功保存日志

```
2025-10-18 20:10:23 [http-nio-8080-exec-2] INFO  c.s.t.service.GradingService - 
  AI判题记录已保存：answerRecordId=13, questionId=2
```

### 失败日志

```
2025-10-18 20:10:23 [http-nio-8080-exec-2] ERROR c.s.t.service.GradingService - 
  保存AI判题记录失败：answerRecordId=13, error=序列化失败
```

---

## ✅ 测试步骤

### 1. 提交主观题

1. 登录学生账号
2. 进入练习模式
3. 选择一道论述题
4. 输入答案并提交
5. 等待AI判题完成

### 2. 查看答题记录

1. 切换到管理员账号
2. 进入"答题记录管理"
3. 应该能看到刚才的答题记录
4. 分数显示正确

### 3. 查看AI判题审核 ✨

1. 进入"AI判题审核"页面
2. **应该能看到新的记录** ✅
3. 记录包含：
   - 题目ID、用户ID
   - 题目内容预览
   - 用户答案预览
   - AI评分
   - 置信度

4. 点击"查看详情"
   - 显示完整的AI反馈
   - 分项得分
   - 优缺点
   - 改进建议

5. 点击"人工复核"（可选）
   - 可以调整分数
   - 添加复核意见

---

## 📈 数据统计

### 统计维度

1. **总判题数**
   ```sql
   SELECT COUNT(*) FROM ai_grading_record
   ```

2. **待审核数**（置信度 < 0.7）
   ```sql
   SELECT COUNT(*) FROM ai_grading_record 
   WHERE ai_confidence < 0.7
   ```

3. **高置信度数**（置信度 >= 0.9）
   ```sql
   SELECT COUNT(*) FROM ai_grading_record 
   WHERE ai_confidence >= 0.9
   ```

4. **平均置信度**
   ```sql
   SELECT AVG(ai_confidence) FROM ai_grading_record
   ```

---

## 🎯 功能价值

### 1. 教师监督

- 教师可以查看所有AI判题结果
- 对低置信度的判题进行人工复核
- 确保评分公正准确

### 2. 质量控制

- 统计AI判题的置信度分布
- 发现AI判题的薄弱环节
- 持续优化判题算法

### 3. 争议处理

- 学生对分数有异议时
- 教师可查看AI的详细评分依据
- 进行人工复核和调整

### 4. 数据分析

- 分析哪些题型AI判题更准确
- 哪些题型需要更多人工介入
- 为AI模型优化提供数据支持

---

## 🔧 后续优化建议

### 1. 批量复核

- 支持选择多条记录批量复核
- 统一调整分数
- 批量通过/驳回

### 2. 复核提醒

- 低置信度的记录自动提醒教师
- 邮件/站内信通知
- 复核截止时间提醒

### 3. 学生申诉

- 学生对AI评分不满意时可以申诉
- 自动转为待复核状态
- 教师审核后给出最终结果

### 4. 统计报表

- AI判题准确率趋势图
- 各题型置信度分布
- 人工复核率统计
- 生成周报/月报

### 5. AI模型对比

- 支持多个AI模型判题
- 对比不同模型的结果
- 选择最优模型

---

## 📋 修改文件清单

### 后端

1. ✅ `src/main/java/com/springboot/tiku/service/GradingService.java`
   - 注入 `AiGradingRecordRepository`
   - 添加 `saveAIGradingRecord()` 方法
   - 添加 `extractAnswerText()` 辅助方法
   - 在判题流程中调用保存方法

### 前端

无需修改（前端已实现）

### 数据库

无需修改（表结构已存在）

---

## 🎉 修复效果

### 修复前

```
管理后台 - AI判题审核
┌─────────────────────┐
│  暂无数据           │  ❌ 空空如也
└─────────────────────┘
```

### 修复后

```
管理后台 - AI判题审核
┌─────────────────────────────────────────┐
│ ID  用户  题目              AI评分  置信度 │
├─────────────────────────────────────────┤
│ 13  1    马克思主义...      4.0/5   80%  │ ✅
│ 11  1    马克思主义...      4.8/5   90%  │ ✅
│ 9   1    马克思主义...      4.0/5   85%  │ ✅
└─────────────────────────────────────────┘
```

---

**修复完成时间：** 2025-10-18 20:04  
**测试状态：** ⏳ 待用户测试  
**影响范围：** 后端判题流程、AI判题记录管理  
**兼容性：** ✅ 完全兼容，无破坏性修改





