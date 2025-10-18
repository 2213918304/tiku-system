# 章节管理页面前后端对接完成说明

## 完成时间
2025-10-17

## 主要修改内容

### 1. 字段名对齐修复

#### 问题
前端页面使用 `orderNum` 字段，但后端使用 `sortOrder` 字段。

#### 解决方案
将前端所有 `orderNum` 改为 `sortOrder`：

**修改位置**：
- 表单字段定义：`chapterForm.orderNum` → `chapterForm.sortOrder`
- 表单验证规则：`orderNum` → `sortOrder`  
- 表单元素绑定：`v-model="chapterForm.orderNum"` → `v-model="chapterForm.sortOrder"`
- 方法中的字段引用：所有 `orderNum` 改为 `sortOrder`

### 2. parentId 处理统一

#### 问题
- 前端发送 `null` 表示顶级章节
- 后端期望 `0` 表示顶级章节

#### 解决方案

**1. 提交时转换（前端 → 后端）**：
```javascript
const data = {
  subjectId: chapterForm.subjectId!,
  parentId: chapterForm.parentId || 0,  // null/undefined 转换为 0
  name: chapterForm.name,
  sortOrder: chapterForm.sortOrder,
  description: chapterForm.description || null
}
```

**2. 编辑时转换（后端 → 前端）**：
```javascript
// 后端 0 表示顶级章节，前端表单使用 undefined 表示未选择父章节
chapterForm.parentId = (chapter.parentId && chapter.parentId !== 0) 
  ? chapter.parentId 
  : undefined
```

**3. 过滤条件更新**：
```javascript
// 父章节选项（只显示一级章节）
const parentChapterOptions = computed(() => {
  return chapters.value.filter(c => !c.parentId || c.parentId === 0)
})
```

**4. 模板判断更新**：
```vue
<el-tag v-if="!data.parentId || data.parentId === 0" 
        type="success" size="small">
  父章节
</el-tag>
```

### 3. 章节树形结构

#### 实现方式

**前端**：
- 使用 `el-tree` 组件展示树形结构
- 调用 `chapterApi.getTree(subjectId)` 获取树形数据
- 通过 `:props="{ label: 'name', children: 'children' }"` 配置节点

**后端**：
- `ChapterService.getChapterTree()` 方法构建树形结构
- 顶级章节：`parentId == null || parentId == 0`
- 子章节：`parentId > 0`
- 递归构建子节点关系

### 4. 完整的 CRUD 功能

#### ✅ 功能列表

1. **查看章节树**
   - 按学科查看章节层级结构
   - 显示题目数量统计
   - 区分父章节和子章节

2. **添加章节**
   - 添加顶级章节（根章节）
   - 添加子章节（指定父章节）
   - 自动排序

3. **编辑章节**
   - 修改章节名称
   - 修改排序号
   - 修改描述信息
   - **注意**：编辑时学科不可改

4. **删除章节**
   - 删除前检查是否有子章节
   - 删除前检查是否有题目
   - 级联删除提示

5. **树形操作**
   - 悬停显示操作按钮
   - 支持展开/折叠
   - 默认全部展开

## 后端接口说明

### 主要接口

1. **GET /chapters/subject/{subjectId}/tree** - 获取章节树
   - 路径参数：学科ID
   - 返回：`List<ChapterDTO>`（树形结构）

2. **POST /chapters** - 创建章节
   - 请求体：`ChapterRequest`
     - `subjectId`: 学科ID（必填）
     - `parentId`: 父章节ID（0 表示顶级章节）
     - `name`: 章节名称（必填）
     - `sortOrder`: 排序号（默认 0）
     - `description`: 描述
   - 返回：`ChapterDTO`

3. **PUT /chapters/{id}** - 更新章节
   - 路径参数：章节ID
   - 请求体：`ChapterRequest`
   - 返回：`ChapterDTO`

4. **DELETE /chapters/{id}** - 删除章节
   - 路径参数：章节ID
   - 返回：void
   - 限制：
     - 不能删除有子章节的章节
     - 不能删除有题目的章节

## 数据结构

### ChapterDTO

```java
{
  "id": 1,
  "subjectId": 1,
  "name": "第一章 马克思主义的产生和发展",
  "parentId": 0,  // 0 或 null 表示顶级章节
  "level": 1,
  "sortOrder": 1,
  "description": "章节描述",
  "questionCount": 15,
  "createdAt": "2025-10-17T21:30:00",
  "children": [  // 子章节数组
    {
      "id": 2,
      "subjectId": 1,
      "name": "第一节 马克思主义的创立",
      "parentId": 1,
      "level": 2,
      "sortOrder": 1,
      "questionCount": 5,
      "children": []
    }
  ]
}
```

## 使用说明

1. **访问页面**：`http://localhost:5173/admin/chapters`

2. **查看章节**：
   - 从下拉列表选择学科
   - 自动加载该学科的章节树

3. **添加顶级章节**：
   - 点击页面顶部"添加章节"按钮
   - 填写章节信息，不选择父章节
   - 提交

4. **添加子章节**：
   - 在树节点上悬停，点击"添加子章节"
   - 系统自动选择当前章节为父章节
   - 填写子章节信息
   - 提交

5. **编辑章节**：
   - 在树节点上悬停，点击"编辑"
   - 修改章节信息
   - **注意**：学科字段已禁用，不可修改
   - 提交

6. **删除章节**：
   - 在树节点上悬停，点击"删除"
   - 确认删除操作
   - 如果有子章节或题目，会提示无法删除

## 关键技术点

### 1. 树形数据处理

后端使用递归算法构建树形结构：

```java
private ChapterDTO buildChapterTree(Chapter chapter, 
                                   Map<Long, List<Chapter>> childrenMap) {
    ChapterDTO dto = convertToDTO(chapter);
    
    List<Chapter> children = childrenMap.get(chapter.getId());
    if (children != null && !children.isEmpty()) {
        List<ChapterDTO> childrenDTOs = children.stream()
            .map(child -> buildChapterTree(child, childrenMap))
            .collect(Collectors.toList());
        dto.setChildren(childrenDTOs);
    } else {
        dto.setChildren(new ArrayList<>());
    }
    
    return dto;
}
```

### 2. parentId 的双重表示

后端同时支持两种方式表示顶级章节：
- `parentId == null`
- `parentId == 0`

过滤顶级章节的条件：
```java
.filter(c -> c.getParentId() == null || c.getParentId() == 0)
```

### 3. 删除保护机制

```java
// 检查是否有子章节
List<Chapter> children = chapterRepository
    .findByParentIdOrderBySortOrderAsc(id);
if (!children.isEmpty()) {
    throw new BusinessException("该章节下有子章节，不能删除");
}

// 检查是否有题目
if (chapter.getQuestionCount() > 0) {
    throw new BusinessException("该章节下有题目，不能删除");
}
```

## 样式特性

1. **悬停交互**：
   - 操作按钮默认隐藏（opacity: 0）
   - 鼠标悬停时显示（opacity: 1）
   - 平滑过渡动画

2. **标签显示**：
   - 题目数量标签（灰色）
   - 父章节标签（绿色）

3. **树形样式**：
   - 默认全部展开
   - 支持折叠/展开
   - 清晰的层级关系

## 测试建议

### 基础功能测试

1. ✅ 选择学科，查看章节树
2. ✅ 添加顶级章节
3. ✅ 添加子章节（二级、三级）
4. ✅ 编辑章节名称和信息
5. ✅ 删除章节（空章节）
6. ✅ 尝试删除有子章节的章节（应提示错误）
7. ✅ 尝试删除有题目的章节（应提示错误）

### 边界情况测试

1. 未选择学科时的提示
2. 排序号的最小最大值（0-999）
3. 章节名称长度限制
4. 刷新功能
5. 表单验证

### 数据完整性测试

1. 章节树的正确构建
2. 题目数量统计准确性
3. 父子关系正确性
4. 排序功能是否生效

## 注意事项

1. **parentId 规则**：
   - 前端表单中，不选择父章节 = 顶级章节
   - 提交时转换为 `0`
   - 编辑时，`0` 转换为 `undefined`（不显示在父章节下拉框中）

2. **学科选择**：
   - 添加时可选择学科
   - 编辑时学科禁用（不允许改变章节所属学科）

3. **删除限制**：
   - 必须先删除子章节
   - 必须先删除或移动章节下的所有题目

4. **排序**：
   - `sortOrder` 越小越靠前
   - 建议使用 0, 1, 2, 3... 或 10, 20, 30... 便于后续插入

## 后续优化建议

1. **拖拽排序**：实现树节点的拖拽重新排序
2. **批量操作**：支持批量删除、批量移动
3. **章节移动**：允许更改章节的父章节
4. **导入导出**：支持章节结构的导入导出
5. **复制章节**：快速复制章节结构
6. **章节统计**：显示更详细的章节统计信息
7. **搜索功能**：在章节树中搜索

## 相关文件

### 前端
- `tiku-frontend/src/views/admin/Chapters.vue` - 页面组件
- `tiku-frontend/src/api/modules/chapter.ts` - API 接口
- `tiku-frontend/src/types/index.ts` - 类型定义

### 后端
- `src/main/java/com/springboot/tiku/controller/ChapterController.java` - 控制器
- `src/main/java/com/springboot/tiku/service/ChapterService.java` - 服务层
- `src/main/java/com/springboot/tiku/dto/chapter/ChapterDTO.java` - 响应DTO
- `src/main/java/com/springboot/tiku/dto/chapter/ChapterRequest.java` - 请求DTO
- `src/main/java/com/springboot/tiku/entity/Chapter.java` - 实体类






