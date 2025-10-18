# 题目顺序修复说明

## 问题描述
导入题目后，题目列表的顺序是倒序的，最新导入的题目在最上面，而不是题号1在最上面。

## 问题原因
默认排序规则设置为按 `createdAt` 降序（desc），导致最新创建的题目显示在最前面。

## 解决方案

### 1. 修改后端默认排序规则
**文件**: `src/main/java/com/springboot/tiku/controller/QuestionController.java`

将默认排序从 `createdAt desc` 改为 `id asc`：

```java
@PostMapping("/search")
public Result<Page<QuestionDTO>> searchQuestions(
        @RequestBody QuestionQueryRequest queryRequest,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "id") String sortBy,           // 改为id
        @RequestParam(defaultValue = "asc") String direction        // 改为asc
) {
    Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) 
        ? Sort.Direction.ASC : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    return Result.success(questionService.getQuestions(queryRequest, pageable));
}
```

**效果**: 
- 题目列表默认按ID升序排列
- 导入的第一题（ID最小）会显示在最上面
- 题号1始终在列表顶部

### 2. 前端也使用相同的排序规则
**文件**: `tiku-frontend/src/views/subjects/Index.vue`

确保前端调用时明确指定排序规则：

```typescript
const res = await questionApi.list({
  subjectId,
  page: 0,
  size: 10000,
  sortBy: 'id',
  direction: 'asc' // 按ID升序排序，小号在前
})
```

## 排序逻辑说明

### ID自增规则
- 数据库中题目的ID是自增的
- 先导入的题目ID小，后导入的题目ID大
- 因此按ID升序排列就是按导入顺序排列

### 用户体验
- ✅ 题号1在最上面
- ✅ 题号按顺序递增显示
- ✅ 符合用户阅读习惯
- ✅ 与Excel导入顺序一致

## 其他排序选项

如果用户需要其他排序方式，可以在前端添加排序选择器：

```typescript
// 可选的排序方式
const sortOptions = [
  { label: '题号升序', sortBy: 'id', direction: 'asc' },
  { label: '题号降序', sortBy: 'id', direction: 'desc' },
  { label: '最新创建', sortBy: 'createdAt', direction: 'desc' },
  { label: '最早创建', sortBy: 'createdAt', direction: 'asc' },
  { label: '难度从低到高', sortBy: 'difficulty', direction: 'asc' },
  { label: '难度从高到低', sortBy: 'difficulty', direction: 'desc' }
]
```

## 测试建议

1. ✅ 清空现有题目数据
2. ✅ 按顺序导入题目（题号1、2、3...）
3. ✅ 访问题库浏览页面
4. ✅ 确认题号1在最上面
5. ✅ 确认题号按升序排列
6. ✅ 测试分页功能是否正常

## 注意事项

1. **重启后端服务**: 修改了后端代码后需要重启
2. **清除缓存**: 建议清除浏览器缓存或硬刷新（Ctrl+F5）
3. **数据一致性**: 如果之前导入过题目，建议重新导入以确保顺序正确
4. **ID不连续**: 如果删除过题目，ID可能不连续，这是正常的

## 验证步骤

### 方法1：查看数据库
```sql
SELECT id, title, created_at 
FROM question 
WHERE subject_id = ? 
ORDER BY id ASC 
LIMIT 10;
```

### 方法2：前端验证
1. 打开浏览器开发者工具（F12）
2. 切换到 Network 标签
3. 刷新页面
4. 查看 `/api/questions/search` 请求
5. 检查返回的题目顺序

### 方法3：界面验证
- 直接查看页面显示的题号
- 第一题应该显示题号1
- 题号应该递增（1、2、3、4...）

## 总结

修改后：
- ✅ 默认排序改为按ID升序
- ✅ 导入顺序与显示顺序一致
- ✅ 题号1始终在最上面
- ✅ 符合用户预期

请重启后端服务后测试！




