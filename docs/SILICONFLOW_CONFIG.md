# 硅基流动（SiliconCloud）AI判题配置指南

## 📝 概述

本系统集成了**硅基流动（SiliconCloud）**的AI能力，用于主观题（简答题、论述题、案例分析题等）的智能判题。

硅基流动官方文档：https://docs.siliconflow.cn/cn/userguide/quickstart

## 🔑 获取API密钥

### 步骤1：注册账号

1. 访问 [SiliconCloud官网](https://cloud.siliconflow.cn/)
2. 点击右上角"登录"按钮
3. 支持以下登录方式：
   - 短信登录
   - 邮箱登录
   - GitHub OAuth
   - Google OAuth

### 步骤2：创建API Key

1. 登录后访问 [API密钥页面](https://cloud.siliconflow.cn/account/ak)
2. 点击"新建API密钥"
3. 保存生成的API Key（注意：只显示一次，请妥善保管）

## ⚙️ 配置系统

### 1. 修改配置文件

编辑 `src/main/resources/application.yml` 文件，找到AI配置部分：

```yaml
# AI配置 - 硅基流动（SiliconCloud）
ai:
  siliconflow:
    # API密钥（必填）- 从 https://cloud.siliconflow.cn/account/ak 获取
    api-key: YOUR_API_KEY_FROM_CLOUD_SILICONFLOW_CN  # 🔴 替换为你的API Key
    # API基础URL（必填）
    base-url: https://api.siliconflow.cn/v1
    # 默认模型（可选）- 推荐使用 Qwen2.5 系列
    model: Qwen/Qwen2.5-7B-Instruct
    # 温度参数（0-1）- 控制生成的随机性，越低越确定
    temperature: 0.3
    # 最大token数 - 控制回复长度
    max-tokens: 2000
    # 请求超时时间（毫秒）
    timeout: 30000
  # AI判题配置
  grading:
    enabled: true  # 是否启用AI判题
    confidence-threshold: 0.75  # 置信度阈值，低于此值转人工复核
    retry-times: 2  # 重试次数
    cache-enabled: true  # 是否缓存判题结果
```

### 2. 配置项说明

| 配置项 | 说明 | 默认值 | 是否必填 |
|--------|------|--------|----------|
| `api-key` | 硅基流动API密钥 | - | ✅ 是 |
| `base-url` | API基础地址 | `https://api.siliconflow.cn/v1` | ✅ 是 |
| `model` | 使用的模型 | `Qwen/Qwen2.5-7B-Instruct` | ❌ 否 |
| `temperature` | 温度参数（0-1） | `0.3` | ❌ 否 |
| `max-tokens` | 最大token数 | `2000` | ❌ 否 |
| `timeout` | 请求超时时间（毫秒） | `30000` | ❌ 否 |

## 🤖 推荐模型

根据不同场景选择合适的模型：

### 1. 通用判题（推荐）
- **Qwen/Qwen2.5-7B-Instruct**
  - 平衡性能和成本
  - 适合大多数判题场景
  - 响应速度快

### 2. 高质量判题
- **Qwen/Qwen2.5-72B-Instruct**
  - 更高的判题准确性
  - 更详细的评语
  - 适合重要考试

### 3. 推理模型（高级场景）
- **Pro/deepseek-ai/DeepSeek-R1**
  - 强大的推理能力
  - 适合复杂的案例分析题
  - 成本较高

更多可用模型请查看：[模型广场](https://cloud.siliconflow.cn/models)

## 💡 使用示例

### 配置示例1：基础配置（使用默认模型）

```yaml
ai:
  siliconflow:
    api-key: sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    base-url: https://api.siliconflow.cn/v1
    model: Qwen/Qwen2.5-7B-Instruct
    temperature: 0.3
```

### 配置示例2：高质量判题配置

```yaml
ai:
  siliconflow:
    api-key: sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    base-url: https://api.siliconflow.cn/v1
    model: Qwen/Qwen2.5-72B-Instruct  # 使用更大的模型
    temperature: 0.2  # 降低随机性，提高稳定性
    max-tokens: 3000  # 增加token数，获得更详细的评语
```

### 配置示例3：推理模型配置

```yaml
ai:
  siliconflow:
    api-key: sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    base-url: https://api.siliconflow.cn/v1
    model: Pro/deepseek-ai/DeepSeek-R1
    temperature: 0.3
    max-tokens: 4000
    timeout: 60000  # 推理模型可能需要更长时间
```

## 🔍 验证配置

### 方法1：通过日志验证

启动项目后，查看日志中是否有AI服务初始化信息：

```
INFO  c.s.tiku.service.ai.SiliconFlowAIService : 硅基流动AI服务初始化成功
DEBUG c.s.tiku.service.ai.SiliconFlowAIService : 调用硅基流动API：model=Qwen/Qwen2.5-7B-Instruct
```

### 方法2：提交一道主观题测试

1. 创建一道简答题并启用AI判题
2. 提交答案
3. 查看判题结果和AI反馈

## ⚠️ 常见问题

### Q1: API Key无效

**错误信息**：`401 Unauthorized` 或 `Invalid API Key`

**解决方法**：
1. 确认API Key是否正确复制（包括前缀 `sk-`）
2. 检查API Key是否已过期或被删除
3. 重新生成API Key

### Q2: 请求超时

**错误信息**：`TimeoutException`

**解决方法**：
1. 增加 `timeout` 配置值
2. 检查网络连接
3. 选择响应更快的模型

### Q3: 判题结果不准确

**解决方法**：
1. 调整 `temperature` 参数（降低值提高稳定性）
2. 使用更大的模型（如72B）
3. 优化评分标准和参考答案
4. 降低 `confidence-threshold`，让更多结果转人工复核

### Q4: AI服务费用问题

**说明**：
- 硅基流动按API调用次数和token使用量计费
- 查看详细价格：[模型价格](https://cloud.siliconflow.cn/models)
- 建议设置使用限额，避免超支

## 📊 费用优化建议

1. **选择合适的模型**
   - 日常练习：使用7B模型
   - 正式考试：使用72B模型

2. **启用缓存**
   - 相同题目和答案的判题结果会被缓存
   - 减少重复调用

3. **设置置信度阈值**
   - 提高阈值可减少人工复核
   - 但可能影响判题准确性

4. **批量判题**
   - 在非高峰期进行批量判题
   - 可能享受更优惠的价格

## 🔗 相关链接

- [硅基流动官网](https://cloud.siliconflow.cn/)
- [官方文档](https://docs.siliconflow.cn/)
- [模型广场](https://cloud.siliconflow.cn/models)
- [API密钥管理](https://cloud.siliconflow.cn/account/ak)
- [定价信息](https://cloud.siliconflow.cn/pricing)

## 📞 技术支持

如有问题，可以：
1. 查看[官方文档](https://docs.siliconflow.cn/)
2. 查看本项目的 [GitHub Issues](https://github.com/your-repo/issues)
3. 联系项目维护者

---

**配置完成后，重启应用即可启用AI判题功能！** 🎉



