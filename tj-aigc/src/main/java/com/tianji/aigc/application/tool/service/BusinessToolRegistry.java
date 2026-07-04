package com.tianji.aigc.application.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianji.aigc.domain.tool.constant.ToolStatus;
import com.tianji.aigc.domain.tool.constant.ToolType;
import com.tianji.aigc.domain.tool.model.ToolEntity;
import com.tianji.aigc.domain.tool.model.config.ToolDefinition;
import com.tianji.aigc.infrastructure.utils.JsonUtils;
import com.tianji.aigc.mapper.ToolMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessToolRegistry {

    private final ToolMapper toolMapper;
    private final DataSource dataSource;

    private final Map<String, ToolDefinition> toolDefinitionCache = new ConcurrentHashMap<>();

    private static final String TOOL_COURSE = "tool_course";
    private static final String TOOL_ORDER = "tool_order";

    @PostConstruct
    public void init() {
        migrateDatabase();
        try {
            registerBusinessTools();
            loadToolDefinitions();
        } catch (Exception e) {
            log.warn("工具注册初始化失败，可能是数据库表结构不完整: {}", e.getMessage());
        }
    }

    private void migrateDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            String[] sqls = {
                "ALTER TABLE tools ADD COLUMN failed_step_status VARCHAR(32) NULL",
                "ALTER TABLE tools ADD COLUMN mcp_server_name VARCHAR(128) NULL",
                "ALTER TABLE tools ADD COLUMN is_global TINYINT(1) DEFAULT 0"
            };
            for (String sql : sqls) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.execute();
                    log.info("数据库迁移成功: {}", sql);
                } catch (Exception e) {
                    log.debug("数据库列可能已存在: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("数据库迁移失败", e);
        }
    }

    private void registerBusinessTools() {
        registerCourseTool();
        registerOrderTool();
    }

    private void registerCourseTool() {
        LambdaQueryWrapper<ToolEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ToolEntity::getId, TOOL_COURSE);
        ToolEntity existing = toolMapper.selectOne(wrapper);

        if (existing == null) {
            ToolEntity courseTool = new ToolEntity();
            courseTool.setId(TOOL_COURSE);
            courseTool.setName("课程查询工具");
            courseTool.setDescription("查询天机学堂课程信息的工具，可以根据课程ID获取课程详情");
            courseTool.setToolType(ToolType.BUILTIN);
            courseTool.setStatus(ToolStatus.PUBLISHED);
            courseTool.setIsOffice(true);
            courseTool.setIsGlobal(true);

            ToolDefinition definition = new ToolDefinition();
            definition.setName("queryCourseById");
            definition.setDescription("根据课程ID查询课程信息，返回课程名称、简介、价格、讲师等信息");
            definition.addRequiredParameter("courseId", "Long", "课程ID");

            List<ToolDefinition> defs = new ArrayList<>();
            defs.add(definition);
            courseTool.setToolList(defs);
            toolMapper.insert(courseTool);
            log.info("课程查询工具注册成功");
        }
    }

    private void registerOrderTool() {
        LambdaQueryWrapper<ToolEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ToolEntity::getId, TOOL_ORDER);
        ToolEntity existing = toolMapper.selectOne(wrapper);

        if (existing == null) {
            ToolEntity orderTool = new ToolEntity();
            orderTool.setId(TOOL_ORDER);
            orderTool.setName("订单预下单工具");
            orderTool.setDescription("课程订单预下单工具，用于确认订单信息和价格");
            orderTool.setToolType(ToolType.BUILTIN);
            orderTool.setStatus(ToolStatus.PUBLISHED);
            orderTool.setIsOffice(true);
            orderTool.setIsGlobal(true);

            ToolDefinition definition = new ToolDefinition();
            definition.setName("prePlaceOrder");
            definition.setDescription("预下单接口，用于在用户确认购买课程前查看订单详情和价格");
            definition.addRequiredParameter("courseIds", "List", "课程ID列表");

            List<ToolDefinition> defs = new ArrayList<>();
            defs.add(definition);
            orderTool.setToolList(defs);
            toolMapper.insert(orderTool);
            log.info("订单预下单工具注册成功");
        }
    }

    private void loadToolDefinitions() {
        LambdaQueryWrapper<ToolEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ToolEntity::getStatus, ToolStatus.PUBLISHED);
        List<ToolEntity> tools = toolMapper.selectList(wrapper);

        for (ToolEntity tool : tools) {
            try {
                List<ToolDefinition> definitions = tool.getToolList();
                if (definitions != null) {
                    for (ToolDefinition def : definitions) {
                        toolDefinitionCache.put(def.getName(), def);
                    }
                }
            } catch (Exception e) {
                log.error("加载工具定义失败: {}", tool.getId(), e);
            }
        }
        log.info("共加载 {} 个工具定义", toolDefinitionCache.size());
    }

    public ToolDefinition getToolDefinition(String toolName) {
        return toolDefinitionCache.get(toolName);
    }

    public List<ToolDefinition> getAllToolDefinitions() {
        return new ArrayList<>(toolDefinitionCache.values());
    }

    public List<ToolDefinition> getToolDefinitionsByNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return new ArrayList<>();
        }
        return names.stream()
                .map(toolDefinitionCache::get)
                .filter(def -> def != null)
                .collect(Collectors.toList());
    }

    public String getToolBeanName(String toolName) {
        if ("queryCourseById".equals(toolName)) {
            return "courseTools";
        } else if ("prePlaceOrder".equals(toolName)) {
            return "orderTools";
        }
        return null;
    }
}
