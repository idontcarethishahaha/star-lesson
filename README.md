
# 天骄星课堂StarLesson

一个基于 **Spring Cloud Alibaba** 的教育行业 AI 智能客服平台——不只是被动问答，还能通过多智能体协作实现课程推荐、购买咨询、知识库检索等全链路智能服务。Spring AI + LangChain4j 双框架协同，Qdrant 向量数据库驱动 RAG 检索，流式 SSE 响应带来流畅对话体验。

---

## Quickstart

需要 **JDK 17**、**Maven 3.8+**、**MySQL 8.x**、**Redis**、**Nacos**、**Qdrant**、**MinIO**。

```bash
git clone &lt;this-repo&gt;
```

**1. 启动基础服务**

确保以下服务已就绪：

| 服务 | 端口 | 说明 |
|------|------|------|
| Nacos | 8848 | 服务注册与配置中心 |
| MySQL | 3306 | 业务数据存储 |
| Redis | 6379 | 缓存 + 对话记忆 |
| Qdrant | 6333 | 向量数据库 |
| MinIO | 9000 | 对象存储（文档/图片） |

**2. 初始化数据库**

```bash
# 导入数据库脚本
mysql -u root -p &lt; sql/schema.sql
```

**3. 配置 Nacos**

将 `nacos` 目录下的配置文件导入 Nacos并设置命名空间 。

**4. 配置环境变量**

```bash
# Windows PowerShell
$env:DASHSCOPE_API_KEY="sk-你的APIKey"

# Linux / macOS
export DASHSCOPE_API_KEY=sk-你的APIKey
```

**5. 启动服务**

```bash
# 按顺序启动（通过 IDEA 或 Maven）
1. tj-gateway        # 网关服务 :8080
2. tj-auth           # 认证服务
3. tj-user           # 用户服务
4. tj-course         # 课程服务
5. tj-aigc           # AI 智能体服务 :8094
6. tj-chat           # 普通对话服务
# ... 其他业务服务按需启动
```

启动后访问 `http://localhost:8080` 进入门户端，`http://localhost:8080/admin` 进入管理端。

---

## 系统全景

```
用户请求 → Gateway（鉴权/路由）──→ 业务服务群
                                  ├─ tj-aigc（AI 智能体）
                                  │      │
                                  │      ├─ RouteAgent（意图路由）
                                  │      ├─ RecommendAgent（课程推荐）
                                  │      ├─ BuyAgent（购买咨询）
                                  │      ├─ RAG 知识库检索
                                  │      └─ 对话记忆系统
                                  │
                                  ├─ tj-chat（普通对话）
                                  ├─ tj-course（课程管理）
                                  ├─ tj-user（用户中心）
                                  ├─ tj-trade（交易订单）
                                  └─ ...
```

| 想看什么 | 文档 |
|---------|------|
| 多智能体架构怎么设计的、Agent 怎么协作 | [tj-aigc Agent 架构](#多智能体系统) |
| RAG 知识库怎么构建和检索 | [RAG 知识检索](#rag-知识检索) |
| 对话记忆策略有哪些、怎么切换 | [对话记忆系统](#对话记忆系统) |
| 工具调用怎么实现的、结果怎么返回前端 | [Function Calling](#function-calling) |
| 流式响应怎么实现的 | [流式响应](#流式响应) |

---

## 多智能体系统

收到消息 → 路由 Agent 判断意图 → 分发到专业 Agent → 工具调用 / RAG 检索 → 流式回复。

### Agent 列表

| Agent | 职责 | 关键能力 |
|-------|------|---------|
| **RouteAgent** | 意图识别与路由 | 根据用户问题判断应该由哪个专业 Agent 处理 |
| **RecommendAgent** | 课程推荐 | RAG 知识库检索 + 课程推荐工具调用 |
| **BuyAgent** | 购买咨询 | 订单查询、优惠券、支付引导等工具调用 |

### 架构设计

基于 **模板方法模式**：`AbstractAgent` 定义统一处理流程（系统提示词组装 → RAG 上下文构建 → 工具配置 → 记忆注入 → ChatClient 调用 → 流式响应处理），各子类实现差异化逻辑。

```java
public interface Agent {
    Flux&lt;ChatEventVO&gt; processStream(String question, String sessionId);
    String process(String question, String sessionId);
    AgentTypeEnum getAgentType();
    void stop(String sessionId);

    default String systemMessage() { return ""; }
    default String buildRagContext(String question) { return ""; }
    default Object[] tools() { return EMPTY_OBJECTS; }
}
```

每个 Agent 独立配置：
- **System Prompt**：通过 Nacos 配置中心动态下发（`route-agent-system-message.txt` 等）
- **RAG 知识库**：各自绑定不同的向量集合
- **工具集**：通过 `@Tool` 注解注册，按需加载

---

## RAG 知识检索

### 完整流程

**入库阶段**：
```
文档上传（MinIO）→ 文档解析 → 文本分块 → Embedding 向量化 → Qdrant 存储
```

支持文档格式：Markdown、Word（.docx）、PDF

**检索阶段**：
```
用户问题 → Embedding 向量化 → Qdrant 相似度检索 → 上下文拼接 → System Prompt 注入
```

检索参数：
- **TopK**: 6
- **相似度阈值**: 0.6
- **Embedding 模型**: text-embedding-3

### 双框架协同

Spring AI 与 LangChain4j 各有优势，通过适配器模式整合：

```java
@Bean
public VectorStore vectorStore(EmbeddingStore&lt;TextSegment&gt; embeddingStore,
                                EmbeddingModel embeddingModel) {
    return new LangChain4jEmbeddingStoreAdapter(embeddingStore, embeddingModel);
}
```

- **Spring AI**：负责 Agent 调度、Function Calling、ChatClient 流式调用
- **LangChain4j**：负责文档处理、向量存储（Qdrant）、Embedding 集成

---

## Function Calling

### 工具调用流程

1. Agent 启动时扫描 `@Tool` 注解的方法，自动注册为可用工具
2. Spring AI 将工具描述（Schema）发送给 LLM
3. LLM 判断需要调用工具时，返回函数名 + 参数
4. Spring AI 通过反射执行工具方法
5. 工具结果双通道返回：
   - **LLM 通道**：结果交给 LLM 生成自然语言回复
   - **PARAM 通道**：结构化数据通过 SSE PARAM 事件直接推给前端渲染

### ToolResultHolder 双通道

工具调用返回的课程列表、订单信息等结构化数据，如果只通过 LLM 转成自然语言，前端无法做卡片渲染。

`ToolResultHolder` 用 ThreadLocal 存储工具执行的结构化结果，流式响应中检测到工具执行完成时，额外推送一个 PARAM 事件，前端拿到 JSON 数据直接渲染课程卡片/订单卡片。

---

## 对话记忆系统

两种记忆策略，实现 `ChatMemory` 接口，可通过配置切换：

### MessageWindow（滑动窗口）

保留最近 N 条消息（默认 100 条），超出后丢弃最早的消息。适合短对话场景，响应快、无额外 Token 消耗。

### SummarizingChatMemory（自动摘要）

当对话 Token 超过阈值（4000）时，自动触发摘要压缩：
- 保留最近 5 条原始消息
- 将更早的历史交给 LLM 生成摘要
- 摘要 + 近期消息 共同作为上下文

适合长对话场景，平衡上下文长度与信息保留。

### 存储方式

| 存储类型 | 适用场景 | 说明 |
|---------|---------|------|
| Redis | 热数据 | 正在进行的对话，读写快 |
| MySQL | 冷数据 | 对话结束后归档持久化 |

通过配置 `tj.ai.memory.type` 切换：`Redis` / `MYSQL`。

---

## 流式响应

基于 **SSE + Reactor Flux** 实现流式输出。

### 为什么用 SSE 不用 WebSocket

SSE 是服务端单向推送，实现简单、兼容性好，完全满足 LLM 流式输出场景。WebSocket 双向通信的能力在这里用不上，SSE 更轻量。

### 事件类型

| 事件 | 说明 |
|------|------|
| `MESSAGE` | LLM 生成的文本 Token |
| `PARAM` | 工具调用的结构化结果 |
| `FINISH` | 生成结束信号 |
| `ERROR` | 异常信息 |

### 中断机制

用户点击「停止」→ 更新 Redis 中会话状态 → 取消 Flux 订阅 → LLM 调用中断，避免 Token 浪费。

---

## 服务模块

| 模块 | 说明 | 端口 |
|------|------|------|
| `tj-gateway` | API 网关，统一鉴权、路由、限流 | 8080 |
| `tj-auth` | 认证服务，JWT 签发与校验 | - |
| `tj-user` | 用户中心，账号、个人信息 | - |
| `tj-course` | 课程管理，课程、分类、教师 | - |
| `tj-aigc` | **AI 智能体服务**（核心） | 8094 |
| `tj-chat` | 普通对话服务（LangChain4j） | - |
| `tj-trade` | 交易订单 | - |
| `tj-pay` | 支付服务 | - |
| `tj-learning` | 学习中心，听课、笔记 | - |
| `tj-exam` | 考试测评 | - |
| `tj-media` | 媒资服务，视频点播 | - |
| `tj-message` | 消息中心，短信、站内信 | - |
| `tj-promotion` | 营销活动、优惠券 | - |
| `tj-search` | 搜索服务（ES） | - |
| `tj-data` | 数据统计 | - |
| `tj-remark` | 评论系统 | - |

前端：
- `tj-front/tj-admin`：管理后台（Vue 3 + Element Plus）
- `tj-front/tj-protal`：用户门户（Vue 3 + Element Plus）

---

## 技术栈

### 后端
- **框架**：Spring Boot 3.3、Spring Cloud 2023.0.3、Spring Cloud Alibaba 2023.0.3.2
- **AI**：Spring AI 1.0.0、LangChain4j 0.29.1、DashScope SDK
- **数据库**：MySQL 8.0、MyBatis-Plus 3.5.9
- **缓存**：Redis + Redisson
- **向量库**：Qdrant
- **消息队列**：RocketMQ
- **服务治理**：Nacos、Sentinel、Seata
- **任务调度**：XXL-Job
- **对象存储**：MinIO
- **文档处理**：Apache PDFBox、Apache POI、Flexmark

### 前端
- **框架**：Vue 3、Vite
- **UI 库**：Element Plus
- **工具**：Axios、Pinia、Vue Router

---

## 数据库

核心数据库：

| 数据库 | 说明 |
|--------|------|
| `tj_aigc` | AI 平台表（agents、agent_versions、tools、rag_datasets 等） |
| `tj_chat` | 对话记录表 |
| `tj_course` | 课程库 |
| `tj_user` | 用户库 |
| `tj_trade` | 交易库 |

初始化脚本见 `sql/` 目录，AI 平台初始化脚本见 `tj-aigc/src/main/resources/sql/ai_platform_init.sql`。

---

## 工作区

所有运行时数据：

| 数据 | 位置 |
|------|------|
| 上传文档 | MinIO `tj-aigc` bucket |
| 向量数据 | Qdrant 集合 |
| 对话记忆 | Redis / MySQL |
| 业务数据 | MySQL 各库 |
| 日志 | 各服务 `logs/` 目录 |

