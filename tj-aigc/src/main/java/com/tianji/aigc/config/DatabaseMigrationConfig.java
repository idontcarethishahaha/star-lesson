package com.tianji.aigc.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigrationConfig {

    private final DataSource dataSource;

    @PostConstruct
    public void migrate() {
        try (Connection conn = dataSource.getConnection()) {
            createUserSessionTable(conn);
            createAgentScheduledTaskTable(conn);
            log.info("数据库迁移完成");
        } catch (Exception e) {
            log.warn("数据库迁移失败: {}", e.getMessage());
        }
    }

    private void createUserSessionTable(Connection conn) {
        String sql = """
            CREATE TABLE IF NOT EXISTS user_session (
                id BIGINT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                name VARCHAR(128),
                tag VARCHAR(64),
                session_id VARCHAR(128),
                create_time DATETIME,
                INDEX idx_user_id (user_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
            log.info("user_session 表创建成功");
        } catch (Exception e) {
            log.debug("user_session 表可能已存在: {}", e.getMessage());
        }
    }

    private void createAgentScheduledTaskTable(Connection conn) {
        String sql = """
            CREATE TABLE IF NOT EXISTS agent_scheduled_task (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                user_id BIGINT NOT NULL,
                agent_id VARCHAR(128),
                task_name VARCHAR(128),
                task_type VARCHAR(64),
                cron_expression VARCHAR(64),
                message_template TEXT,
                enabled TINYINT(1) DEFAULT 1,
                last_fire_time DATETIME,
                next_fire_time DATETIME,
                created_at DATETIME,
                updated_at DATETIME,
                INDEX idx_user_id (user_id),
                INDEX idx_enabled (enabled)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
            log.info("agent_scheduled_task 表创建成功");
        } catch (Exception e) {
            log.debug("agent_scheduled_task 表可能已存在: {}", e.getMessage());
        }
    }
}