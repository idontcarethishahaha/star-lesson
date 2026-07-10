# 天骄星课堂智能体项目 (tjxt) V3

> 更新时间：2026-07-06
>
> 本文档基于当前代码现状整理，重点记录 AI 智能体模块在 V2 之后的架构调整与实现细节。

---

## 一、项目概述

天骄星课堂智能体项目（tjxt）是一个基于 **Spring Cloud Alibaba 微服务架构**的在线教育平台，核心业务覆盖课程管理、AI 智能问答、课程推荐、在线交易、学习记录、考试测评等。平台最大的技术特色是采用了 **Spring AI + LangChain4j 双 AI 框架**：

- **tj-aigc**：基于 Spring AI 1.0 构建的企业级 AI 智能体平台，支持多智能体协作、RAG 知识库、Function Calling、对话记忆、Agent 市场等。
- **tj-chat**：基于 LangChain4j 0.29 构建的个人学习助手，支持流式对话、Markdown 知识库问答、向量化检索。

### 1.1 技术栈

| 分类 | 技术 |
|------|------|
| 基础框架 | Spring Boot 3.3.5 / Spring Cloud 2023.0.3 / Spring Cloud Alibaba 2023.0.3.2 |
| JDK | 17（tj-aigc）/ 11（tj-chat） |
| 服务治理 | Nacos（注册中心 + 配置中心）、Gateway、OpenFeign、Sentinel |
| AI 框架 | Spring AI 1.0.0 + Spring AI Alibaba 1.0.0.2 / LangChain4j 0.29.1 |
| 大模型 | 智谱 GLM-4.5-Flash（主模型）、通义千问 DashScope（备用/音频） |
| Embedding | embedding-3（1024 维） |
| 向量数据库 | Qdrant（HTTP 6333 / gRPC 6334） |
| 文件存储 | MinIO（192.168.227.128:9000） |
| 消息队列 | RocketMQ |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis |
| 分布式事务 | Seata 1.5.1 |
| 任务调度 | XXL-Job 2.3.1 |
| 搜索 | Elasticsearch 7.12.1 |
| 文档解析 | Flexmark（Markdown）、PDFBox（PDF）、Apache POI（Word） |

---

## 二、项目结构

```
tjxt/
├── tj-aigc/              # AI 智能体核心服务 ⭐⭐⭐
├── tj-api/               # API 定义层（Feign 接口）
├── tj-auth/              # 认证服务
│   ├── tj-auth-gateway-sdk/   # 网关鉴权 SDK
│   └── tj-auth-resource-sdk/  # 资源鉴权 SDK
├── tj-common/            # 公共模块
├── tj-course/            # 课程服务
├── tj-data/              # 数据服务
├── tj-exam/              # 考试服务
├── tj-front/             # 前端项目
│   ├── tj-admin/         # 管理端前端
│   └── tj-protal/        # 学生端前端
├── tj-gateway/           # 网关服务
├── tj-learning/          # 学习服务
├── tj-media/             # 媒体服务
├── tj-message/           # 消息服务
├── tj-pay/               # 支付服务
├── tj-promotion/         # 推广服务
├── tj-remark/            # 评价服务
├── tj-search/            # 搜索服务
├── tj-trade/             # 交易服务
├── tj-user/              # 用户服务
├── tj-chat/              # LangChain4j 个人学习助手
├── nacos/                # Nacos 配置文件
└── sql/                  # 数据库脚本
```

---

## 三、AI 智能体核心服务 (tj-aigc)

### 3.1 服务信息

| 属性 | 值 |
|------|-----|
| 服务名 | aigc-service |
| 端口 | 8094 |
| 注册地址 | 192.168.227.128:8848（namespace: star-lesson-dev，group: sl-group） |
| 数据库 | tj_aigc |
| 主类 | com.tianji.AIGCApplication |

### 3.2 分层架构（DDD）

```
tj-aigc/src/main/java/com/tianji/aigc/
├── agent/                    # 系统内置 Agent（模板方法模式）
│   ├── Agent.java           # Agent 接口
│   ├── AbstractAgent.java   # Agent 抽象基类
│   ├── RouteAgent.java      # 路由智能体
│   ├── RecommendAgent.java  # 推荐智能体
│   ├── ConsultAgent.java    # 咨询智能体
│   └── BuyAgent.java        # 购买智能体
├── application/              # 应用层
│   ├── agent/              # Agent 应用服务（CRUD、版本、发布）
│   ├── chat/               # Agent 聊天应用服务
│   ├── llm/                # LLM 模型管理
│   ├── rag/                # RAG 知识库应用服务
│   └── tool/               # 工具市场应用服务
├── config/                  # 配置类（SpringAI、Qdrant、MinIO、提示词等）
├── controller/              # 早期聊天接口层（ChatController 等）
├── domain/                  # 领域层
│   ├── agent/              # Agent 领域模型
│   ├── llm/                # LLM 领域模型
│   ├── rag/                # RAG 领域模型
│   └── tool/               # 工具领域模型
├── infrastructure/          # 基础设施层（异常、转换器、初始化器、限流、敏感词）
├── interfaces/              # 接口层（RESTful API）
│   └── controller/
│       ├── AgentController.java
│       ├── AgentChatController.java
│       ├── RagController.java
│       ├── ToolController.java
│       ├── LLMController.java
│       └── McpServerController.java
├── mapper/                  # MyBatis-Plus Mapper
├── memory/                  # 对话记忆实现 ⭐
├── service/                 # 早期聊天服务（ChatService 体系）
├── tools/                   # 业务工具（@Tool 注解）
└── utils/                   # 工具类
```

### 3.3 核心功能模块

#### 3.3.1 系统内置 Agent（三种对话模式）

通过 `tj.ai.chat-type` 配置切换：

| 模式 | 说明 | 实现类 |
|------|------|--------|
| ENHANCE | 单智能体增强模式（默认），一个 ChatClient 处理所有问题 | ChatServiceImpl |
| ROUTE | 路由模式，先由 RouteAgent 识别意图，再分发到领域 Agent | AgentServiceImpl |
| APP | 阿里云百炼模式，直接调用百炼预置应用 | AppAgentChatService |

> V3 重点调整：RAG 检索从 `QuestionAnswerAdvisor` 自动注入模式，改为 **Agent 自定义 `buildRagContext()` 方法手动拼接上下文**，每个 Agent / 自定义 Agent 可独立配置知识库 ID、相似度阈值、TopK。

#### 3.3.2 Agent 抽象层（模板方法模式）

`Agent` 接口定义扩展点：

```java
public interface Agent {
    Flux<ChatEventVO> processStream(String question, String sessionId);  // 流式
    String process(String question, String sessionId);                   // 非流式
    AgentTypeEnum getAgentType();                                        // 类型
    void stop(String sessionId);                                         // 停止生成
    default String systemMessage() { return ""; }                        // 系统提示词
    default String buildRagContext(String question) { return ""; }       // RAG 上下文
    default Object[] tools() { return EMPTY_OBJECTS; }                   // 工具列表
    default Map<String, Object> toolContext(String sessionId, String requestId) { return Map.of(); }
    default List<Advisor> advisors() { return List.of(); }
    default Map<String, Object> advisorParams(String sessionId, String requestId) { return Map.of(); }
    default Map<String, Object> systemMessageParams() { return Map.of(); }
}
```

`AbstractAgent` 固化通用流程：生成 requestId → 更新会话 → 组装 ChatClientRequest（system + RAG 上下文 + advisors + tools + toolContext + user） → 流式输出 → 停止状态管理 → ToolResultHolder 返回。

#### 3.3.3 自定义 Agent 平台（数据库驱动）

通过 `AgentAppService` + `AgentChatService` 实现，核心实体：

| 实体 | 说明 |
|------|------|
| `AgentEntity` / `agents` | Agent 主表，记录系统提示词、工具 ID、知识库 ID、发布版本等 |
| `AgentVersionEntity` / `agent_versions` | Agent 版本表，支持草稿/审核中/已发布/拒绝/已下架状态 |
| `ToolEntity` / `tools` | 工具市场主表，支持 MCP / BUILTIN 类型 |
| `UserToolEntity` / `user_tools` | 用户安装的工具 |
| `UserRagEntity` / `user_rags` | 用户安装的 RAG 版本快照 |
| `AgentScheduledTaskEntity` / `agent_scheduled_tasks` | Agent 定时任务 |

预置 Agent（启动时由 `PresetAgentInitializer` 初始化）：

| Agent ID | 名称 | 工具 | 知识库 |
|----------|------|------|--------|
| preset_route | 路由智能体 | 无 | 无 |
| preset_recommend | 课程推荐智能体 | courseTools | 可配置 |
| preset_consult | 课程咨询智能体 | courseTools | 可配置 |
| preset_buy | 课程购买智能体 | orderTools, courseTools | 无 |

路由 Agent 输出格式（JSON）：

```json
{
  "agentName": "RECOMMEND",
  "confidence": 0.95,
  "reason": "用户询问课程推荐"
}
```

#### 3.3.4 RAG 知识库

**处理流程：**

```
上传文档 → 文本提取（PDF/Word/Markdown/TXT） → 分块处理 → Embedding → Qdrant 存储
                                                           ↓
用户问题 → Embedding → Qdrant 检索（按 dataset_id 过滤） → 上下文组装 → 拼入系统提示词 → LLM 生成回答
```

**核心配置（AIProperties.Rag）：**

```yaml
tj:
  ai:
    prompt:
      # ...
    rag:
      similarityThreshold: 0.6
      topK: 6
      recommendKnowledgeBaseIds:
        - xxx
      consultKnowledgeBaseIds:
        - yyy
```

**核心类：**

| 类 | 职责 |
|----|------|
| `RagAppService` | 知识库数据集、文件、版本的应用服务 |
| `RagVectorService` | 文档分块、向量化、向量检索、向量删除 |
| `RagSearchService` | 封装检索请求，生成上下文字符串 |
| `QdrantConfig` / `QdrantProperties` | Qdrant 客户端配置 |
| `LangChain4jConfig` | LangChain4j EmbeddingModel / EmbeddingStore |
| `LangChain4jEmbeddingStoreAdapter` | 将 LangChain4j EmbeddingStore 适配为 Spring AI VectorStore |

**分块策略：**

- 最大块大小：800 字符
- 重叠大小：100 字符
- 按标点符号（。！？.!?\n；;）智能断句
- Markdown  additionally supports heading-level splitting in tj-chat

**检索策略：**

- 默认相似度阈值：0.6
- 默认 TopK：6
- 支持按 `dataset_id` 多库联合检索（should 条件）
- 检索失败返回空字符串，不影响主对话流程

#### 3.3.5 Function Calling 工具体系

基于 Spring AI `@Tool` 注解实现的业务工具：

| 工具类 | 功能 | 调用服务 |
|--------|------|---------|
| `CourseTools` | 课程查询、课程详情 | course-service |
| `OrderTools` | 预下单、订单查询 | trade-service |
| `LearningTools` | 学习记录、课程完成 | learning-service |
| `StudyPlanTools` | 学习计划管理 | learning-service |
| `CheckInTools` | 学习打卡 | learning-service |
| `LearningReportTools` | 学习报告生成 | learning-service |

**ToolContext 传递：**

- `REQUEST_ID`：用于关联一次完整请求
- `USER_ID`：当前登录用户 ID，解决工具调用时的身份透传

**ToolResultHolder 双通道设计：**

工具执行结果同时服务于两个目标：

1. 作为 Function Calling 返回值给 LLM，继续推理生成文本；
2. 存入 `ToolResultHolder`（ConcurrentHashMap，key = requestId），流式输出结束时通过 `PARAM` 事件推送给前端，用于渲染课程卡片、订单确认等富交互组件。

#### 3.3.6 对话记忆系统

`SpringAIConfig` 中装配，支持两种策略：

| 策略 | 说明 | 配置 |
|------|------|------|
| MessageWindow | 滑动窗口，按消息数限制 | `tj.ai.memory.strategy=message_window`（默认） |
| Summarizing | 摘要压缩，Token 超阈值自动摘要 | `tj.ai.memory.strategy=summarizing` |

**存储后端可配置：**

- Redis：`tj.ai.memory.type=Redis`
- MySQL：`tj.ai.memory.type=MYSQL`

`SummarizingChatMemory` 核心逻辑：

1. 当对话总 token 超过 `summaryThreshold`（默认 4000）时触发；
2. 将消息列表对半切分；
3. 前半段交由 LLM 生成摘要（区分首次摘要 / 增量摘要）；
4. 摘要保存到 `conversationId + "_summary"`；
5. 下次 `get()` 时，摘要作为 SystemMessage 拼在最前面；
6. 摘要失败时降级为「保留最近 20 条消息」。

**conversationId 设计：**

```java
static String getConversationId(String sessionId) {
    return UserContext.getUser() + "_" + sessionId;
}
```

通过 userId + sessionId 组合，确保不同用户的同名会话不冲突。

#### 3.3.7 流式对话与停止机制

- 基于 **SSE（Server-Sent Events）** + **Reactor Flux** 实现流式输出；
- 每个 token 包装为 `ChatEventVO`，事件类型 `DATA`；
- 生成状态通过 **Redis Hash**（`GENERATE_STATUS`）中心化管理，支持跨实例停止；
- `takeWhile` 操作符每 token 检查 Redis 状态，状态被删除则终止流；
- `doOnCancel` 钩子保存已生成内容到 `ChatMemory`，避免用户中断后丢失半截回答；
- 工具结果通过 `PARAM` 事件推送，最后发送 `STOP` 事件标识结束。

#### 3.3.8 系统提示词热更新

`SystemPromptConfig` 通过 Nacos Config Listener + `AtomicReference` 实现：

- 启动时从 Nacos 加载提示词；
- Nacos 配置变更时自动替换引用；
- 加载失败时使用默认提示词，保证服务可用；
- 提示词支持 `{now}` 等参数占位符。

#### 3.3.9 模型配置

通过 `SpringAIConfig` 装配两个 ChatClient：

| Bean | 模型 | 用途 |
|------|------|------|
| `chatClient` | DashScope ChatModel | 主对话、Agent 对话 |
| `openAiChatClient` | OpenAI 兼容接口（智谱 GLM） | 非流式文本聊天、备用 |

LangChain4j 模型配置（用于 Embedding 和 tj-chat）：

```yaml
langchain4j:
  base-url: https://open.bigmodel.cn/api/paas/v4
  api-key: ${DASHSCOPE_API_KEY}
  model-name: glm-4.5-flash
  embedding-model-name: embedding-3
  max-tokens: 2000
  timeout-seconds: 60
  max-retries: 3
  chat-model-temperature: 0.7
```

### 3.4 关键 API 接口

#### 内置 Agent 接口（ChatController）

```
GET  /chat/           # SSE 流式聊天（chat-type 配置决定模式）
GET  /chat/file       # 知识库聊天
GET  /chat/records    # 聊天记录
POST /chat/stop       # 停止生成
POST /chat/text       # 非流式聊天
```

#### 自定义 Agent 接口（AgentChatController）

```
POST /agent-chat              # SSE 与指定 Agent 对话
POST /agent-chat/stop         # 停止生成
POST /agent-chat/text         # 非流式与 Agent 对话
GET  /agent-chat/welcome      # 获取 Agent 欢迎语
```

#### Agent 管理平台接口（AgentController 等）

```
POST   /agents                    # 创建 Agent
PUT    /agents/{id}               # 更新 Agent
DELETE /agents/{id}               # 删除 Agent
GET    /agents/{id}               # 查询 Agent
GET    /agents                    # 列表
POST   /agents/{id}/versions      # 创建版本
POST   /agents/{id}/versions/publish   # 发布版本
POST   /agents/{id}/versions/offline   # 下架版本
POST   /agents/{id}/tools         # 绑定工具
POST   /agents/{id}/knowledge-bases    # 绑定知识库
```

---

## 四、LangChain4j 学习助手 (tj-chat)

### 4.1 服务信息

| 属性 | 值 |
|------|-----|
| 服务名 | chat-service |
| 端口 | 8095 |
| JDK | 11 |
| 数据库 | tj_chat |
| 主类 | com.tianji.chat.ChatApplication |

### 4.2 核心能力

基于 LangChain4j 的 `AiServices` 接口模式：

```java
public interface AssistantRedis {
    String chat(@MemoryId String memoryId, @UserMessage String message);

    @SystemMessage("你叫小天，是StarLesson的智能学习助手...")
    TokenStream stream(@MemoryId String memoryId, @UserMessage String message);

    List<ChatMessage> getHistory(@MemoryId String memoryId);
}
```

### 4.3 知识库问答

- 用户上传 Markdown 文档；
- `MarkdownSplitter` 按标题级别（H2/H3/智能最大标题）分块；
- `EmbeddingModel` 生成向量；
- 存储到 Qdrant，metadata 包含 `user_id`、`doc_id`；
- 问答时按 `user_id` 过滤检索 Top 3；
- 上下文用 `[SYS_CONTEXT_BEGIN]` / `[SYS_CONTEXT_END]` 标记包裹；
- 流式返回给前端（SseEmitter）。

### 4.4 数据表

| 表 | 说明 |
|----|------|
| `chat_session` | 聊天会话分片记录 |
| `user_session` | 用户与会话关联表 |
| `user_markdown_docs` | 用户上传的 Markdown 文档 |

---

## 五、数据库说明

### 5.1 数据库列表

由 `sql/schema.sql` 初始化：

```sql
CREATE DATABASE IF NOT EXISTS tj_auth;
CREATE DATABASE IF NOT EXISTS tj_chat;
CREATE DATABASE IF NOT EXISTS tj_course;
CREATE DATABASE IF NOT EXISTS tj_data;
CREATE DATABASE IF NOT EXISTS tj_exam;
CREATE DATABASE IF NOT EXISTS tj_learning;
CREATE DATABASE IF NOT EXISTS tj_media;
CREATE DATABASE IF NOT EXISTS tj_message;
CREATE DATABASE IF NOT EXISTS tj_pay;
CREATE DATABASE IF NOT EXISTS tj_promotion;
CREATE DATABASE IF NOT EXISTS tj_remark;
CREATE DATABASE IF NOT EXISTS tj_search;
CREATE DATABASE IF NOT EXISTS tj_trade;
CREATE DATABASE IF NOT EXISTS tj_user;
CREATE DATABASE IF NOT EXISTS tj_aigc;
```

### 5.2 tj-aigc 核心表

详见 `tj-aigc/src/main/resources/sql/ai_platform_init.sql`：

| 表 | 说明 |
|----|------|
| `providers` | LLM 服务商 |
| `models` | 模型配置 |
| `agents` | Agent 主表 |
| `agent_versions` | Agent 版本 |
| `tools` | 工具市场 |
| `tool_versions` | 工具版本 |
| `user_tools` | 用户安装的工具 |
| `ai_rag_qa_dataset` | RAG 知识库数据集 |
| `file_detail` | 上传文件详情 |
| `document_unit` | 文档单元（分块后） |
| `rag_versions` / `rag_version_files` / `rag_version_documents` | RAG 版本快照 |
| `user_rags` | 用户安装的 RAG |
| `chat_sessions` | 会话 |
| `messages` | 消息 |
| `context` | 上下文（滑动窗口/摘要） |
| `memory_items` | 长期记忆项 |
| `agent_scheduled_tasks` | Agent 定时任务 |
| `api_keys` | API 密钥 |

### 5.3 tj-chat 核心表

详见 `sql/tj_chat.sql`：

| 表 | 说明 |
|----|------|
| `chat_session` | 聊天片段（JSON 存储） |
| `user_session` | 用户会话 |
| `user_markdown_docs` | Markdown 文档 |

---

## 六、服务依赖与启动顺序

### 6.1 核心依赖

```
Gateway (10010)
    ↓
    ├─→ AuthService (8081)    # 认证
    ├─→ UserService (8082)    # 用户信息
    ├─→ AIGCService (8094)    # AI 智能体 ⭐
    └─→ ChatService (8095)    # 学习助手 ⭐
            ↓
            ├─→ CourseService (8086)   # 课程查询
            ├─→ LearningService (8090) # 学习进度
            └─→ OrderService (8085)    # 订单
```

### 6.2 启动顺序

1. **Nacos** (192.168.227.128:8848)
2. **MySQL** (192.168.227.128:3306)
3. **Redis** (192.168.227.128:6379)
4. **Qdrant** (192.168.227.128:6333 HTTP / 6334 gRPC)
5. **MinIO** (192.168.227.128:9000)
6. **RocketMQ** (192.168.227.128:9876)
7. **GatewayApplication** (10010)
8. **AuthApplication** (8081)
9. **UserApplication** (8082)
10. **AIGCApplication** (8094)
11. **ChatApplication** (8095)

---

## 七、V2 → V3 关键变更

| 变更点 | V2 | V3 |
|--------|----|----|
| RAG 检索方式 | Spring AI `QuestionAnswerAdvisor` 自动注入 | Agent 自定义 `buildRagContext()` 手动拼接 |
| 知识库配置 | 全局统一 | 按 Agent 独立配置 `knowledgeBaseIds` |
| 自定义 Agent | 仅基础 Agent 管理 | 完整支持 Agent 创建、版本、发布、工具绑定、知识库绑定、对话 |
| 路由 Agent | 返回字符串（如 `RECOMMEND`） | 返回 JSON（agentName + confidence + reason），支持置信度兜底 |
| Agent 对话 | 无 | 新增 `AgentChatService` + `AgentChatController`，动态加载 Agent 配置和工具 |
| ChatClient 工具注入 | 固定工具 | `AgentChatClientFactory` 动态按工具 Bean 名称注入 |
| 工具注册 | 简单初始化 | `BusinessToolRegistry` + `BusinessToolInitializer` 自动注册内置工具 |
| 停止生成 | Redis Hash | 抽象到 Agent 接口，所有 Agent 统一实现 |

---

## 八、配置文件说明

### 8.1 tj-aigc 本地配置

文件：`tj-aigc/src/main/resources/application.yml`

关键配置：

```yaml
server:
  port: 8094
spring:
  application:
    name: aigc-service
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        options:
          model: glm-4.5-flash
    openai:
      api-key: ${DASHSCOPE_API_KEY}
      base-url: https://open.bigmodel.cn/api/paas/v4
      chat:
        options:
          model: glm-4.5-flash
tj:
  ai:
    prompt:
      system:
        chat:
          data-id: system-chat-message.txt
          group: sl-group
        route-agent:
          data-id: route-agent-system-message.txt
        recommend-agent:
          data-id: recommend-agent-system-message.txt
        buy-agent:
          data-id: buy-agent-system-message.txt
    memory:
      max: 100
      type: Redis
      strategy: message_window   # message_window / summarizing
      maxTokens: 8000
      summaryThreshold: 4000
    chat-type: ENHANCE           # ENHANCE / ROUTE / APP
  minio:
    enabled: true
    endpoint: http://192.168.227.128:9000
    bucket: tj-aigc
  qdrant:
    host: 192.168.227.128
    port: 6333
```

### 8.2 tj-chat 本地配置

文件：`tj-chat/src/main/resources/bootstrap.yml`

```yaml
server:
  port: 8095
spring:
  application:
    name: chat-service
  cloud:
    nacos:
      server-addr: 192.168.227.128:8848
      config:
        namespace: star-lesson-dev
        group: sl-group
      discovery:
        namespace: star-lesson-dev
        group: sl-group
langchain4j:
  base-url: https://open.bigmodel.cn/api/paas/v4
  api-key: ${DASHSCOPE_API_KEY}
  model-name: glm-4.5-flash
  embedding-model-name: embedding-3
qdrant:
  host: 192.168.227.128
  port: 6334
```

---

## 九、开发注意事项

1. **API Key 配置**：需要在环境变量、IDE Run Configuration 或项目根目录 `.env` 中设置 `DASHSCOPE_API_KEY`，否则启动后 LLM 调用会失败。
2. **Qdrant 集合**：首次使用时会自动创建集合，向量维度为 1024。
3. **JDK 版本**：tj-aigc 使用 JDK 17，tj-chat 使用 JDK 11，编译时注意分别配置。
4. **MySQL 驱动冲突**：tj-chat 中 MySQL 驱动排除了 `protobuf-java`，避免与 Qdrant 客户端版本冲突。
5. **LangChain4j 版本**：tj-aigc 和 tj-chat 均使用 0.29.1，注意保持一致性。
6. **RAG 上下文位置**：V3 将 RAG 上下文拼接到系统提示词后面（或前面），不再通过 `QuestionAnswerAdvisor` 隐式注入。
7. **工具 Bean 名称**：自定义 Agent 通过 `toolIds` 映射到 Spring Bean 名称（如 `courseTools`、`orderTools`），映射逻辑在 `AgentChatService.convertToolIdsToBeanNames()`。

---

## 十、相关文件索引

| 文件 | 说明 |
|------|------|
| `pom.xml` | 根 POM，版本管理 |
| `tj-aigc/pom.xml` | AI 模块依赖 |
| `tj-aigc/src/main/java/com/tianji/aigc/agent/AbstractAgent.java` | Agent 模板方法基类 |
| `tj-aigc/src/main/java/com/tianji/aigc/application/chat/service/AgentChatService.java` | 自定义 Agent 对话核心 |
| `tj-aigc/src/main/java/com/tianji/aigc/application/rag/service/RagVectorService.java` | 向量化与检索 |
| `tj-aigc/src/main/java/com/tianji/aigc/application/rag/service/RagSearchService.java` | RAG 上下文生成 |
| `tj-aigc/src/main/java/com/tianji/aigc/config/SpringAIConfig.java` | Spring AI Bean 装配 |
| `tj-aigc/src/main/java/com/tianji/aigc/memory/SummarizingChatMemory.java` | 摘要式记忆 |
| `tj-aigc/src/main/java/com/tianji/aigc/infrastructure/initializer/PresetAgentInitializer.java` | 预置 Agent 初始化 |
| `tj-aigc/src/main/resources/sql/ai_platform_init.sql` | AI 平台数据库初始化脚本 |
| `tj-chat/src/main/java/com/tianji/chat/config/AiConfig.java` | LangChain4j Assistant 配置 |
| `tj-chat/src/main/java/com/tianji/chat/service/impl/ChatSessionServiceImpl.java` | 流式对话实现 |
