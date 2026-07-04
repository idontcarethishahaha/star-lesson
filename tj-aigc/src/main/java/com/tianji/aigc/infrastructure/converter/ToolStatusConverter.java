package com.tianji.aigc.infrastructure.converter;

import com.tianji.aigc.domain.tool.constant.ToolStatus;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(ToolStatus.class)
public class ToolStatusConverter extends JsonToStringConverter<ToolStatus> {

    public ToolStatusConverter() {
        super(ToolStatus.class);
    }

    @Override
    protected ToolStatus parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return ToolStatus.fromCode(json.replaceAll("\"", ""));
    }
}
