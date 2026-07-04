-- 用户上传的 Markdown 文档表
CREATE TABLE IF NOT EXISTS `user_markdown_docs` (
  `id` BIGINT NOT NULL COMMENT '主键，自增',
  `user_id` BIGINT NOT NULL COMMENT '上传用户ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '上传时的原始文件名',
  `content` LONGTEXT NOT NULL COMMENT '整个 Markdown 文本内容',
  `level` INT DEFAULT 2 COMMENT '文档切割等级',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户上传的Markdown文档表';
