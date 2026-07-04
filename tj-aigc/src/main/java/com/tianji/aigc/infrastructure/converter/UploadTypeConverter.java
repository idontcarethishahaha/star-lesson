package com.tianji.aigc.infrastructure.converter;

import com.tianji.aigc.domain.tool.constant.UploadType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(UploadType.class)
public class UploadTypeConverter extends JsonToStringConverter<UploadType> {

    public UploadTypeConverter() {
        super(UploadType.class);
    }

    @Override
    protected UploadType parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return UploadType.fromCode(json.replaceAll("\"", ""));
    }
}
