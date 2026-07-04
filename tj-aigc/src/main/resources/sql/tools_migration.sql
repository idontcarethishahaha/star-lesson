-- 为 tools 表添加缺失字段
ALTER TABLE tools ADD COLUMN failed_step_status VARCHAR(32) NULL COMMENT '失败步骤状态' AFTER reject_reason;
ALTER TABLE tools ADD COLUMN mcp_server_name VARCHAR(128) NULL COMMENT 'MCP服务名称' AFTER failed_step_status;
ALTER TABLE tools ADD COLUMN is_global TINYINT(1) DEFAULT 0 COMMENT '是否全局工具' AFTER mcp_server_name;
