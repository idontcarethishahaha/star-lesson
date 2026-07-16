# AI 系统架构详解

## 一、AI 系统整体工作流程

```
用户输入
    ↓
[请求预处理] - 提取意图、用户身份、上下文
    ↓
[智能路由] - 根据意图选择对应的 Agent
    ↓
[Agent 执行] - RAG检索 + 工具调用 + LLM推理
    ↓
[结果整合] - 格式化输出，保持对话状态
    ↓
[返回用户] - 流式推送结果
```

## 二、四大核心组件

### 1. Multi-Agent 多智能体系统
**作用**：将复杂任务分解为多个专业智能体协作

**四种 Agent 类型**：
- **路由 Agent** - 理解用户意图，选择合适的 Agent
- **咨询 Agent** - 课程咨询、学习答疑
- **推荐 Agent** - 个性化课程推荐
- **购课 Agent** - 购买决策、订单处理

### 2. RAG 检索增强系统
**作用**：让 AI 能访问外部知识库，避免"幻觉"

**检索流程**：
```
用户问题 → 向量相似度搜索 → Qdrant检索 → 过滤排序 → 
 relevance_score > threshold 的文档 → 拼入Prompt → LLM生成
```

### 3. Function Calling 工具系统
**作用**：让 AI 能执行具体的业务操作

**工具分类**：
- **课程工具** - 查询课程大纲、师资、评价
- **交易工具** - 生成订单、查询订单状态
- **学情工具** - 学习进度、成绩分析

### 4. 记忆管理系统
**作用**：维护对话上下文，支持长对话

**记忆策略**：
- **短期记忆**：最近 N 轮对话（滑动窗口）
- **长期记忆**：对话摘要（增量压缩）

## 三、关键技术细节

### Spring AI + LangChain4j 双框架协作

| 框架 | 职责 | 优势 |
|------|------|------|
| Spring AI | 统一接口、工具封装、配置管理 | 与 Spring 生态深度集成 |
| LangChain4j | Agent 构建、链路编排、组件扩展 | 丰富的 LLM 支持和工具链 |

### 智能体动态配置机制

```yaml
# Agent 配置示例
agent_config:
  id: course_consultant
  name: 课程咨询专家
  version: v1.2
  message_template: "你是专业的课程顾问..."
  tools:
    - search_course
    - get_teacher_info
  rag_config:
    knowledge_base: course_kb
    metadata_filter: 
      subject: "计算机科学"
  context_window: 4096
```

### 工具调用权限控制

```java
// 工具权限示例
@Tool(description = "查询课程信息", 
      permission = "course:read")
public CourseInfo getCourseInfo(String courseId) {
    // 业务逻辑
}

@Tool(description = "创建订单", 
      permission = "order:create")
public Order createOrder(OrderRequest request) {
    // 业务逻辑
}
```

## 四、数据隔离与安全

### 多租户知识库隔离
- 每个客户独立的知识库
- 检索时自动添加租户过滤条件
- 防止知识内容泄露

### 用户身份透传
- 工具调用自动携带用户 ID
- 支持权限精细化控制
- 审计日志记录所有操作

## 五、性能优化

### 并发优化
- 异步向量化线程池（50并发）
- 智能体实例缓存
- RAG 结果缓存

### 流式输出优化
- SSE 流式推送
- 分块生成，首字响应 <200ms
- 支持中断和断点续传

## 六、监控与治理

### 指标监控
- QPS、响应延迟、错误率
- 工具调用成功率
- RAG 检索准确率

### 动态配置
- Nacos 管理提示词配置
- 热更新 Agent 行为
- 动态调整限流策略