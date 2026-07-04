package com.tianji.aigc.infrastructure.converter;

import org.apache.ibatis.type.MappedTypes;
import com.tianji.aigc.domain.tool.model.config.ToolDefinition;
import com.tianji.aigc.infrastructure.utils.JsonUtils;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@MappedTypes(List.class)
public class ToolDefinitionListConverter extends JsonToStringConverter<List<ToolDefinition>> {

    public ToolDefinitionListConverter() {
        super((Class<List<ToolDefinition>>) (Class<?>) List.class);
    }

    @Override
    protected List<ToolDefinition> parseJson(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<ToolDefinition> result = JsonUtils.parseArray(json, ToolDefinition.class);
        return result != null ? result : Collections.emptyList();
    }
}
