package com.tianji.aigc.infrastructure.converter;

import com.tianji.aigc.domain.tool.constant.ToolType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(ToolType.class)
public class ToolTypeConverter extends JsonToStringConverter<ToolType> {

    public ToolTypeConverter() {
        super(ToolType.class);
    }

    @Override
    protected ToolType parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return ToolType.fromCode(json.replaceAll("\"", ""));
    }
}
