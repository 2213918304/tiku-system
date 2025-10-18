# 题目序号功能说明（按章节独立编号）

## 功能描述
每个章节的题目独立编号，从1开始递增。删除题目后，新导入的题目会自动接续该章节的最后一个序号。

## 实现原理

### 1. 数据库字段
在 `Question` 表中添加 `serial_number` 字段：
```sql
ALTER TABLE question ADD COLUMN serial_number INT COMMENT '题目序号（章节内的顺序号）';
```

### 2. 自动计算序号
导入题目时，自动计算序号逻辑：
```java
// 获取该章节当前最大序号
Integer maxSerialNumber = questionRepository.findMaxSerialNumberByChapterId(chapterId);
// 新题目序号 = 最大序号 + 1（如果没有题目，则从1开始）
question.setSerialNumber(maxSerialNumber != null ? maxSerialNumber + 1 : 1);
```

### 3. 排序规则
查询题目时，优先按 `serialNumber` 排序，如果为null则按 `id` 排序：
```java
Sort sort = Sort.by(Sort.Direction.ASC, "serialNumber")
                .and(Sort.by(Sort.Direction.ASC, "id"));
```

## 使用示例

### 场景1：首次导入
**第一章**：导入3道题
- 题目1：serialNumber = 1
- 题目2：serialNumber = 2
- 题目3：serialNumber = 3

**第二章**：导入4道题
- 题目1：serialNumber = 1
- 题目2：serialNumber = 2
- 题目3：serialNumber = 3
- 题目4：serialNumber = 4

### 场景2：删除后再导入
**第一章**：已有序号 1, 2, 3, 4
- 删除序号2的题目
- 现有序号：1, 3, 4
- **新导入2道题**：序号为 **5, 6**（接续最大序号4）

**第二章**：已有序号 1, 2, 3
- 删除序号1, 2的题目
- 现有序号：3
- **新导入3道题**：序号为 **4, 5, 6**（接续最大序号3）

### 场景3：章节内重新排序
如果需要让章节内的序号连续（去除中间删除造成的空号）：
```sql
-- 需要手动执行SQL重新编号（可选）
SET @row_number = 0;
UPDATE question 
SET serial_number = (@row_number:=@row_number+1) 
WHERE chapter_id = ? 
ORDER BY serial_number ASC, id ASC;
```

## 显示效果

### 题库浏览页面
```
📖 第一章 绪论
  1. 根据《辽宁师范大学学生管理规定》...
  2. 学校制定学生违纪处分的具体办法...
  3. 同学小明是大一新生...

📖 第二章 唯物辩证法
  1. 唯物辩证法认为...
  2. 质量互变规律...
  3. 矛盾的普遍性...
```

每个章节的题号都从1开始！

## 优势

### ✅ 符合教材习惯
- 每章都从第1题开始
- 与纸质教材、题库的编号方式一致
- 便于学生理解和记忆

### ✅ 删除友好
- 删除题目不影响其他题目的序号
- 新导入的题目自动接续
- 避免序号冲突

### ✅ 灵活扩展
- 每章可以独立添加题目
- 不影响其他章节的序号
- 支持任意顺序导入

## 数据库查询示例

### 查询章节内的题目
```sql
SELECT serial_number, title, content
FROM question
WHERE chapter_id = 1
ORDER BY serial_number ASC, id ASC;
```

### 获取章节最大序号
```sql
SELECT MAX(serial_number) 
FROM question 
WHERE chapter_id = 1;
```

### 跨章节查询（显示章节名和序号）
```sql
SELECT 
    c.name AS chapter_name,
    q.serial_number,
    q.title
FROM question q
JOIN chapter c ON q.chapter_id = c.id
WHERE q.subject_id = 1
ORDER BY c.sort_order ASC, q.serial_number ASC, q.id ASC;
```

## 前端显示

### 题目列表显示格式
```typescript
// 显示：章节名 + 序号
const displayNumber = `${question.chapterName} 第${question.serialNumber}题`

// 或者简化显示
const displayNumber = question.serialNumber
```

### 按章节分组显示
```typescript
// 将题目按章节分组
const groupedQuestions = questions.reduce((acc, q) => {
  const chapterId = q.chapterId
  if (!acc[chapterId]) {
    acc[chapterId] = {
      chapterName: q.chapterName,
      questions: []
    }
  }
  acc[chapterId].questions.push(q)
  return acc
}, {})

// 每个章节内按序号排序
Object.values(groupedQuestions).forEach(group => {
  group.questions.sort((a, b) => a.serialNumber - b.serialNumber)
})
```

## 注意事项

1. **导入顺序**：按章节顺序导入题目，序号会自动递增
2. **删除影响**：删除题目后会留下序号空缺，这是正常的
3. **重新编号**：如果需要连续的序号，需要手动执行SQL重新编号
4. **智能导入**：智能导入模式会自动创建章节并分配序号
5. **章节迁移**：如果题目移动到其他章节，序号不会自动调整

## 常见问题

### Q：删除题目后序号有空缺怎么办？
A：这是正常现象。如果需要连续序号，可以手动执行重新编号SQL。

### Q：如何查看某章节的题目数量？
A：使用 `COUNT(*)` 查询该章节的题目总数，而不是看最大序号。

### Q：智能导入会自动分配序号吗？
A：是的，智能导入会根据章节信息自动分配序号。

### Q：手动创建题目时序号是多少？
A：需要在创建时指定，或者让系统自动计算（获取该章节最大序号+1）。

## 技术实现

### 后端修改
1. ✅ `Question` 实体添加 `serialNumber` 字段
2. ✅ `QuestionRepository` 添加查询最大序号的方法
3. ✅ `DataImportService` 导入时自动计算序号
4. ✅ `QuestionController` 排序规则改为优先按序号排序

### 前端修改
1. ✅ 题目列表显示序号
2. ✅ 按章节分组显示
3. ✅ 查询时按序号排序

### 数据库迁移
```sql
-- 添加字段
ALTER TABLE question ADD COLUMN serial_number INT COMMENT '题目序号（章节内）';

-- 为现有题目初始化序号（可选）
-- 方案1：按ID顺序为每个章节的题目编号
SET @row_number = 0;
SET @current_chapter = NULL;

UPDATE question q
JOIN (
    SELECT 
        id,
        chapter_id,
        @row_number := IF(@current_chapter = chapter_id, @row_number + 1, 1) AS new_serial,
        @current_chapter := chapter_id
    FROM question
    ORDER BY chapter_id, id
) AS numbered ON q.id = numbered.id
SET q.serial_number = numbered.new_serial;

-- 方案2：只为新导入的题目自动分配序号，旧题目保持为NULL
-- （推荐，避免破坏现有数据）
```

## 总结

实现了按章节独立编号的功能：
- ✅ 每个章节从1开始编号
- ✅ 自动读取章节最后一题的序号
- ✅ 新导入的题目自动接续
- ✅ 删除题目不影响其他题目
- ✅ 符合教材编号习惯

请重启后端服务，然后重新导入题目测试！




