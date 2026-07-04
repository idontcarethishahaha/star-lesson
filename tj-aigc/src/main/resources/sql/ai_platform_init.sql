-- ============================================
-- 天机学堂 AI 平台数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 说明: Agent管理、LLM模型、工具市场、RAG知识库
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 一、LLM 服务商与模型
-- ============================================

-- 1. 服务商表
CREATE TABLE IF NOT EXISTS `providers` (
    `id` VARCHAR(36) NOT NULL COMMENT '服务商ID',
    `user_id` VARCHAR(36) DEFAULT NULL COMMENT '用户ID（官方为NULL）',
    `protocol` VARCHAR(50) NOT NULL COMMENT '协议类型：OPENAI, DASHSCOPE, ANTHROPIC等',
    `name` VARCHAR(100) NOT NULL COMMENT '服务商名称',
    `description` TEXT COMMENT '服务商描述',
    `config` TEXT COMMENT '服务商配置（加密存储）',
    `is_official` TINYINT(1) DEFAULT 0 COMMENT '是否官方：0-否，1-是',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_providers_user_id` (`user_id`),
    KEY `idx_providers_official` (`is_official`),
    KEY `idx_providers_status` (`status`),
    KEY `idx_providers_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM服务商表';

-- 2. 模型表
CREATE TABLE IF NOT EXISTS `models` (
    `id` VARCHAR(36) NOT NULL COMMENT '模型ID',
    `user_id` VARCHAR(36) DEFAULT NULL COMMENT '用户ID（官方为NULL）',
    `provider_id` VARCHAR(36) NOT NULL COMMENT '服务商ID',
    `model_id` VARCHAR(100) NOT NULL COMMENT '模型标识（如 gpt-4）',
    `name` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `model_endpoint` VARCHAR(255) DEFAULT NULL COMMENT '模型端点',
    `description` TEXT COMMENT '模型描述',
    `is_official` TINYINT(1) DEFAULT 0 COMMENT '是否官方：0-否，1-是',
    `type` VARCHAR(20) NOT NULL COMMENT '模型类型：CHAT, EMBEDDING, IMAGE等',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_models_provider_id` (`provider_id`),
    KEY `idx_models_user_id` (`user_id`),
    KEY `idx_models_type` (`type`),
    KEY `idx_models_status` (`status`),
    KEY `idx_models_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型表';

-- ============================================
-- 二、Agent 管理
-- ============================================

-- 3. Agent 表
CREATE TABLE IF NOT EXISTS `agents` (
    `id` VARCHAR(36) NOT NULL COMMENT 'Agent唯一ID',
    `name` VARCHAR(255) NOT NULL COMMENT 'Agent名称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT 'Agent头像URL',
    `description` TEXT COMMENT 'Agent描述',
    `system_prompt` TEXT COMMENT 'Agent系统提示词',
    `welcome_message` TEXT COMMENT '欢迎消息',
    `tool_ids` JSON COMMENT '工具ID列表，JSON数组',
    `knowledge_base_ids` JSON COMMENT '知识库ID列表，JSON数组',
    `published_version` VARCHAR(36) DEFAULT NULL COMMENT '当前发布的版本ID',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `user_id` VARCHAR(36) NOT NULL COMMENT '创建者用户ID',
    `tool_preset_params` JSON COMMENT '预先设置的工具参数',
    `multi_modal` TINYINT(1) DEFAULT 0 COMMENT '是否支持多模态：0-否，1-是',
    `llm_model_config` JSON COMMENT 'LLM模型配置',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_agents_user_id` (`user_id`),
    KEY `idx_agents_enabled` (`enabled`),
    KEY `idx_agents_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent表';

-- 4. Agent 版本表
CREATE TABLE IF NOT EXISTS `agent_versions` (
    `id` VARCHAR(36) NOT NULL COMMENT '版本唯一ID',
    `agent_id` VARCHAR(36) NOT NULL COMMENT '关联的Agent ID',
    `name` VARCHAR(255) NOT NULL COMMENT 'Agent名称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT 'Agent头像URL',
    `description` TEXT COMMENT 'Agent描述',
    `version_number` VARCHAR(20) NOT NULL COMMENT '版本号，如1.0.0',
    `system_prompt` TEXT COMMENT 'Agent系统提示词',
    `welcome_message` TEXT COMMENT '欢迎消息',
    `tool_ids` JSON COMMENT '工具ID列表，JSON数组',
    `knowledge_base_ids` JSON COMMENT '知识库ID列表，JSON数组',
    `change_log` TEXT COMMENT '版本更新日志',
    `publish_status` INT DEFAULT 1 COMMENT '发布状态：1-草稿, 2-审核中, 3-已发布, 4-拒绝, 5-已下架',
    `reject_reason` TEXT COMMENT '审核拒绝原因',
    `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `published_at` DATETIME DEFAULT NULL COMMENT '发布时间',
    `user_id` VARCHAR(36) NOT NULL COMMENT '创建者用户ID',
    `tool_preset_params` JSON COMMENT '预先设置的工具参数',
    `multi_modal` TINYINT(1) DEFAULT 0 COMMENT '是否支持多模态',
    `llm_model_config` JSON COMMENT 'LLM模型配置',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_agent_versions_agent_id` (`agent_id`),
    KEY `idx_agent_versions_user_id` (`user_id`),
    KEY `idx_agent_versions_publish_status` (`publish_status`),
    KEY `idx_agent_versions_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent版本表';

-- ============================================
-- 三、工具市场
-- ============================================

-- 5. 工具表
CREATE TABLE IF NOT EXISTS `tools` (
    `id` VARCHAR(36) NOT NULL COMMENT '工具唯一ID',
    `name` VARCHAR(255) NOT NULL COMMENT '工具名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '工具图标',
    `subtitle` VARCHAR(255) DEFAULT NULL COMMENT '副标题',
    `description` TEXT COMMENT '工具描述',
    `user_id` VARCHAR(36) DEFAULT NULL COMMENT '用户ID（官方为NULL）',
    `labels` JSON COMMENT '标签列表，JSON数组',
    `tool_type` VARCHAR(20) DEFAULT 'MCP' COMMENT '工具类型：MCP, BUILTIN等',
    `upload_type` VARCHAR(20) DEFAULT 'GITHUB' COMMENT '上传方式：GITHUB, ZIP, LOCAL',
    `upload_url` VARCHAR(500) DEFAULT NULL COMMENT '上传URL',
    `install_command` JSON COMMENT '安装命令配置',
    `tool_list` JSON COMMENT '工具定义列表',
    `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT, PUBLISHED, REVIEWING, REJECTED, OFFLINE',
    `is_office` TINYINT(1) DEFAULT 0 COMMENT '是否官方工具：0-否，1-是',
    `reject_reason` TEXT COMMENT '拒绝原因',
    `failed_step_status` VARCHAR(32) DEFAULT NULL COMMENT '失败步骤状态',
    `mcp_server_name` VARCHAR(100) DEFAULT NULL COMMENT 'MCP服务名称',
    `is_global` TINYINT(1) DEFAULT 0 COMMENT '是否全局工具：0-否，1-是',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_tools_user_id` (`user_id`),
    KEY `idx_tools_status` (`status`),
    KEY `idx_tools_office` (`is_office`),
    KEY `idx_tools_tool_type` (`tool_type`),
    KEY `idx_tools_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工具表';

-- 6. 工具版本表
CREATE TABLE IF NOT EXISTS `tool_versions` (
    `id` VARCHAR(36) NOT NULL COMMENT '版本唯一ID',
    `tool_id` VARCHAR(36) NOT NULL COMMENT '关联的工具ID',
    `version` VARCHAR(50) NOT NULL COMMENT '版本号',
    `change_log` TEXT COMMENT '更新日志',
    `tool_list` JSON COMMENT '工具定义列表（该版本的快照）',
    `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_tool_versions_tool_id` (`tool_id`),
    KEY `idx_tool_versions_version` (`version`),
    KEY `idx_tool_versions_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工具版本表';

-- 7. 用户工具表（用户安装的工具）
CREATE TABLE IF NOT EXISTS `user_tools` (
    `id` VARCHAR(36) NOT NULL COMMENT '记录ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `tool_id` VARCHAR(36) NOT NULL COMMENT '工具ID',
    `tool_version_id` VARCHAR(36) DEFAULT NULL COMMENT '工具版本ID',
    `installed_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '安装时间',
    `config` JSON COMMENT '用户配置',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, DISABLED',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tool` (`user_id`, `tool_id`),
    KEY `idx_user_tools_user_id` (`user_id`),
    KEY `idx_user_tools_tool_id` (`tool_id`),
    KEY `idx_user_tools_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户工具表';

-- ============================================
-- 四、RAG 知识库
-- ============================================

-- 8. RAG知识库数据集表
CREATE TABLE IF NOT EXISTS `ai_rag_qa_dataset` (
    `id` VARCHAR(36) NOT NULL COMMENT '数据集ID',
    `name` VARCHAR(255) NOT NULL COMMENT '数据集名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '数据集图标',
    `description` TEXT COMMENT '数据集说明',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `is_public` TINYINT(1) DEFAULT 0 COMMENT '是否公开：0-否，1-是',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_rag_dataset_user_id` (`user_id`),
    KEY `idx_rag_dataset_public` (`is_public`),
    KEY `idx_rag_dataset_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG知识库数据集表';

-- 9. 文件详情表
CREATE TABLE IF NOT EXISTS `file_detail` (
    `id` VARCHAR(36) NOT NULL COMMENT '文件ID',
    `url` VARCHAR(500) DEFAULT NULL COMMENT '文件访问地址',
    `size` BIGINT DEFAULT NULL COMMENT '文件大小，单位字节',
    `filename` VARCHAR(255) DEFAULT NULL COMMENT '文件名称',
    `original_filename` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    `base_path` VARCHAR(255) DEFAULT NULL COMMENT '基础存储路径',
    `path` VARCHAR(500) DEFAULT NULL COMMENT '存储路径',
    `ext` VARCHAR(20) DEFAULT NULL COMMENT '文件扩展名',
    `content_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    `platform` VARCHAR(50) DEFAULT NULL COMMENT '存储平台',
    `th_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图访问路径',
    `object_id` VARCHAR(36) DEFAULT NULL COMMENT '文件所属对象ID',
    `object_type` VARCHAR(50) DEFAULT NULL COMMENT '文件所属对象类型',
    `metadata` JSON COMMENT '文件元数据',
    `hash_info` VARCHAR(255) DEFAULT NULL COMMENT '哈希信息',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `data_set_id` VARCHAR(36) NOT NULL COMMENT '数据集ID',
    `file_page_size` INT DEFAULT NULL COMMENT '总页数',
    `processing_status` INT DEFAULT 0 COMMENT '处理状态：0-已上传,1-OCR处理中,2-OCR完成,3-向量化中,4-完成,5-OCR失败,6-向量化失败',
    `current_ocr_page_number` INT DEFAULT 0 COMMENT '当前OCR处理页数',
    `current_embedding_page_number` INT DEFAULT 0 COMMENT '当前向量化处理页数',
    `ocr_process_progress` DECIMAL(5,2) DEFAULT 0.00 COMMENT 'OCR处理进度百分比',
    `embedding_process_progress` DECIMAL(5,2) DEFAULT 0.00 COMMENT '向量化处理进度百分比',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_file_detail_user_id` (`user_id`),
    KEY `idx_file_detail_data_set_id` (`data_set_id`),
    KEY `idx_file_detail_processing_status` (`processing_status`),
    KEY `idx_file_detail_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件详情表';

-- 10. 文档单元表
CREATE TABLE IF NOT EXISTS `document_unit` (
    `id` VARCHAR(36) NOT NULL COMMENT '主键',
    `file_id` VARCHAR(36) NOT NULL COMMENT '文件ID',
    `page` INT DEFAULT NULL COMMENT '页码',
    `content` TEXT COMMENT '当前页内容',
    `is_vector` TINYINT(1) DEFAULT 0 COMMENT '是否进行向量化：0-否，1-是',
    `is_ocr` TINYINT(1) DEFAULT 0 COMMENT 'OCR识别状态：0-否，1-是',
    `vector_id` VARCHAR(100) DEFAULT NULL COMMENT '向量ID（在向量数据库中的ID）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_document_unit_file_id` (`file_id`),
    KEY `idx_document_unit_page` (`page`),
    KEY `idx_document_unit_is_vector` (`is_vector`),
    KEY `idx_document_unit_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档单元表';

-- 11. RAG版本表（版本快照）
CREATE TABLE IF NOT EXISTS `rag_versions` (
    `id` VARCHAR(36) NOT NULL COMMENT '版本ID',
    `name` VARCHAR(255) NOT NULL COMMENT '快照时的名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '快照时的图标',
    `description` TEXT COMMENT '快照时的描述',
    `user_id` VARCHAR(36) NOT NULL COMMENT '创建者ID',
    `version` VARCHAR(50) NOT NULL COMMENT '版本号',
    `change_log` TEXT COMMENT '更新日志',
    `labels` JSON COMMENT '标签列表',
    `original_rag_id` VARCHAR(36) NOT NULL COMMENT '原始RAG数据集ID',
    `original_rag_name` VARCHAR(255) DEFAULT NULL COMMENT '原始RAG名称',
    `file_count` INT DEFAULT 0 COMMENT '文件数量',
    `total_size` BIGINT DEFAULT 0 COMMENT '总大小（字节）',
    `document_count` INT DEFAULT 0 COMMENT '文档单元数量',
    `publish_status` INT DEFAULT 1 COMMENT '发布状态：1-草稿,2-审核中,3-已发布,4-拒绝,5-已下架',
    `reject_reason` TEXT COMMENT '审核拒绝原因',
    `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `published_at` DATETIME DEFAULT NULL COMMENT '发布时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_rag_versions_user_id` (`user_id`),
    KEY `idx_rag_versions_original_rag_id` (`original_rag_id`),
    KEY `idx_rag_versions_version` (`version`),
    KEY `idx_rag_versions_publish_status` (`publish_status`),
    KEY `idx_rag_versions_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG版本表';

-- 12. RAG版本文件表
CREATE TABLE IF NOT EXISTS `rag_version_files` (
    `id` VARCHAR(36) NOT NULL COMMENT '文件ID',
    `rag_version_id` VARCHAR(36) NOT NULL COMMENT '关联的RAG版本ID',
    `original_file_id` VARCHAR(36) DEFAULT NULL COMMENT '原始文件ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    `file_page_size` INT DEFAULT NULL COMMENT '文件页数',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件存储路径',
    `process_status` INT DEFAULT NULL COMMENT '处理状态',
    `embedding_status` INT DEFAULT NULL COMMENT '向量化状态',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_rag_version_files_rag_version_id` (`rag_version_id`),
    KEY `idx_rag_version_files_original_file_id` (`original_file_id`),
    KEY `idx_rag_version_files_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG版本文件表';

-- 13. RAG版本文档表
CREATE TABLE IF NOT EXISTS `rag_version_documents` (
    `id` VARCHAR(36) NOT NULL COMMENT '文档单元ID',
    `rag_version_id` VARCHAR(36) NOT NULL COMMENT '关联的RAG版本ID',
    `rag_version_file_id` VARCHAR(36) DEFAULT NULL COMMENT '关联的版本文件ID',
    `original_document_id` VARCHAR(36) DEFAULT NULL COMMENT '原始文档单元ID',
    `content` TEXT COMMENT '文档内容',
    `page` INT DEFAULT NULL COMMENT '页码',
    `vector_id` VARCHAR(100) DEFAULT NULL COMMENT '向量ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_rag_version_docs_rag_version_id` (`rag_version_id`),
    KEY `idx_rag_version_docs_rag_version_file_id` (`rag_version_file_id`),
    KEY `idx_rag_version_docs_original_document_id` (`original_document_id`),
    KEY `idx_rag_version_docs_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG版本文档表';

-- 14. 用户RAG表（用户安装的RAG）
CREATE TABLE IF NOT EXISTS `user_rags` (
    `id` VARCHAR(36) NOT NULL COMMENT '安装记录ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `rag_version_id` VARCHAR(36) NOT NULL COMMENT '关联的RAG版本快照ID',
    `name` VARCHAR(255) DEFAULT NULL COMMENT '安装时的名称',
    `description` TEXT COMMENT '安装时的描述',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '安装时的图标',
    `version` VARCHAR(50) DEFAULT NULL COMMENT '版本号',
    `installed_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '安装时间',
    `original_rag_id` VARCHAR(36) NOT NULL COMMENT '原始RAG数据集ID',
    `install_type` VARCHAR(20) DEFAULT 'SNAPSHOT' COMMENT '安装类型：REFERENCE, SNAPSHOT',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, DISABLED',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_rags_user_id` (`user_id`),
    KEY `idx_user_rags_rag_version_id` (`rag_version_id`),
    KEY `idx_user_rags_original_rag_id` (`original_rag_id`),
    KEY `idx_user_rags_install_type` (`install_type`),
    KEY `idx_user_rags_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户RAG表';

-- ============================================
-- 五、会话与消息
-- ============================================

-- 15. 会话表
CREATE TABLE IF NOT EXISTS `chat_sessions` (
    `id` VARCHAR(36) NOT NULL COMMENT '会话ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `agent_id` VARCHAR(36) DEFAULT NULL COMMENT '关联的Agent ID',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '会话标题',
    `type` VARCHAR(20) DEFAULT 'CHAT' COMMENT '会话类型：CHAT, RAG, AGENT',
    `metadata` JSON COMMENT '扩展元数据',
    `last_message_at` DATETIME DEFAULT NULL COMMENT '最后消息时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_chat_sessions_user_id` (`user_id`),
    KEY `idx_chat_sessions_agent_id` (`agent_id`),
    KEY `idx_chat_sessions_type` (`type`),
    KEY `idx_chat_sessions_last_message_at` (`last_message_at`),
    KEY `idx_chat_sessions_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- 16. 消息表
CREATE TABLE IF NOT EXISTS `messages` (
    `id` VARCHAR(36) NOT NULL COMMENT '消息ID',
    `session_id` VARCHAR(36) NOT NULL COMMENT '所属会话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '消息角色：user, assistant, system',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `message_type` VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT, IMAGE, FILE, TOOL_CALL',
    `token_count` INT DEFAULT 0 COMMENT 'Token数量',
    `provider` VARCHAR(50) DEFAULT NULL COMMENT '服务提供商',
    `model` VARCHAR(50) DEFAULT NULL COMMENT '使用的模型',
    `metadata` JSON COMMENT '消息元数据',
    `file_urls` JSON COMMENT '文件URL列表',
    `tool_calls` JSON COMMENT '工具调用信息',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_messages_session_id` (`session_id`),
    KEY `idx_messages_role` (`role`),
    KEY `idx_messages_created_at` (`created_at`),
    KEY `idx_messages_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 17. 上下文表（滑动窗口/摘要）
CREATE TABLE IF NOT EXISTS `context` (
    `id` VARCHAR(36) NOT NULL COMMENT '上下文ID',
    `session_id` VARCHAR(36) NOT NULL COMMENT '所属会话ID',
    `active_messages` JSON COMMENT '活跃消息ID列表',
    `summary` TEXT COMMENT '历史消息摘要',
    `token_count` INT DEFAULT 0 COMMENT '当前Token数',
    `strategy` VARCHAR(20) DEFAULT 'SLIDING_WINDOW' COMMENT '处理策略：SLIDING_WINDOW, SUMMARIZE',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_context_session_id` (`session_id`),
    KEY `idx_context_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上下文表';

-- ============================================
-- 六、长期记忆
-- ============================================

-- 18. 记忆项表
CREATE TABLE IF NOT EXISTS `memory_items` (
    `id` VARCHAR(36) NOT NULL COMMENT '记忆ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `agent_id` VARCHAR(36) DEFAULT NULL COMMENT '关联的Agent ID',
    `type` VARCHAR(20) DEFAULT 'FACT' COMMENT '记忆类型：FACT, PREFERENCE, GOAL, HABIT',
    `content` TEXT NOT NULL COMMENT '记忆内容',
    `importance` INT DEFAULT 5 COMMENT '重要程度：1-10',
    `source` VARCHAR(50) DEFAULT NULL COMMENT '来源',
    `tags` JSON COMMENT '标签',
    `embedding` BLOB COMMENT '向量嵌入（可选）',
    `access_count` INT DEFAULT 0 COMMENT '访问次数',
    `last_accessed_at` DATETIME DEFAULT NULL COMMENT '最后访问时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_memory_user_id` (`user_id`),
    KEY `idx_memory_agent_id` (`agent_id`),
    KEY `idx_memory_type` (`type`),
    KEY `idx_memory_importance` (`importance`),
    KEY `idx_memory_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='长期记忆表';

-- ============================================
-- 七、Agent 定时任务
-- ============================================

-- 19. Agent定时任务表
CREATE TABLE IF NOT EXISTS `agent_scheduled_tasks` (
    `id` VARCHAR(36) NOT NULL COMMENT '任务ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `agent_id` VARCHAR(36) DEFAULT NULL COMMENT '关联的Agent ID',
    `task_name` VARCHAR(255) NOT NULL COMMENT '任务名称',
    `task_type` VARCHAR(50) DEFAULT 'CUSTOM' COMMENT '任务类型：STUDY_REMINDER, DAILY_REVIEW, WEEKLY_REPORT, LEARNING_PLAN, CUSTOM',
    `cron_expression` VARCHAR(100) DEFAULT NULL COMMENT 'Cron表达式',
    `message_template` TEXT COMMENT '消息模板',
    `target_course_id` BIGINT DEFAULT NULL COMMENT '目标课程ID',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `last_fire_time` DATETIME DEFAULT NULL COMMENT '最后触发时间',
    `next_fire_time` DATETIME DEFAULT NULL COMMENT '下次触发时间',
    `fire_count` INT DEFAULT 0 COMMENT '触发次数',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_ast_user_id` (`user_id`),
    KEY `idx_ast_agent_id` (`agent_id`),
    KEY `idx_ast_enabled` (`enabled`),
    KEY `idx_ast_next_fire_time` (`next_fire_time`),
    KEY `idx_ast_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent定时任务表';

-- ============================================
-- 八、API 密钥
-- ============================================

-- 20. API密钥表
CREATE TABLE IF NOT EXISTS `api_keys` (
    `id` VARCHAR(36) NOT NULL COMMENT 'API Key ID',
    `api_key` VARCHAR(64) NOT NULL COMMENT 'API密钥',
    `agent_id` VARCHAR(36) DEFAULT NULL COMMENT '关联的Agent ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '创建者用户ID',
    `name` VARCHAR(100) DEFAULT NULL COMMENT '名称/描述',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `usage_count` INT DEFAULT 0 COMMENT '已使用次数',
    `last_used_at` DATETIME DEFAULT NULL COMMENT '最后使用时间',
    `expires_at` DATETIME DEFAULT NULL COMMENT '过期时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '软删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_api_keys_key` (`api_key`),
    KEY `idx_api_keys_user_id` (`user_id`),
    KEY `idx_api_keys_agent_id` (`agent_id`),
    KEY `idx_api_keys_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API密钥表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 初始化数据
-- ============================================

-- 插入默认服务商（通义千问）
INSERT INTO `providers` (`id`, `protocol`, `name`, `description`, `is_official`, `status`, `config`)
VALUES ('provider_dashscope_default', 'DASHSCOPE', '通义千问', '阿里云通义千问大模型服务', 1, 1, '{}');

-- 插入默认模型
INSERT INTO `models` (`id`, `provider_id`, `model_id`, `name`, `description`, `is_official`, `type`, `status`)
VALUES 
('model_qwen_turbo', 'provider_dashscope_default', 'qwen-turbo', '通义千问 Turbo', '通义千问快速响应模型', 1, 'CHAT', 1),
('model_qwen_plus', 'provider_dashscope_default', 'qwen-plus', '通义千问 Plus', '通义千问增强版模型', 1, 'CHAT', 1),
('model_qwen_max', 'provider_dashscope_default', 'qwen-max', '通义千问 Max', '通义千问最强模型', 1, 'CHAT', 1),
('model_qwen_embedding', 'provider_dashscope_default', 'text-embedding-v2', '通义 embedding', '文本嵌入模型', 1, 'EMBEDDING', 1);

-- 插入默认 Agent（课程推荐助手）
INSERT INTO `agents` (`id`, `name`, `avatar`, `description`, `system_prompt`, `welcome_message`, `enabled`, `user_id`, `multi_modal`)
VALUES ('agent_course_recommend', '课程推荐助手', '', '天机学堂智能课程推荐助手，根据你的需求推荐合适的课程', 
'你是天机学堂的课程推荐助手，你需要：1. 了解用户的学习需求 2. 根据需求推荐合适的课程 3. 协助用户下单购买', 
'你好！我是天机学堂的课程推荐助手 (≧∇≦)ﾉ 有什么课程想了解吗？', 1, 'system', 0);

SELECT 'AI平台数据库初始化完成！' AS message;
