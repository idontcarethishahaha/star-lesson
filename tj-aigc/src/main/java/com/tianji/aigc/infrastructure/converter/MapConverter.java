package com.tianji.aigc.infrastructure.converter;

import org.apache.ibatis.type.MappedTypes;
import java.util.Map;

@MappedTypes(Map.class)
public class MapConverter extends JsonToStringConverter<Map> {

    public MapConverter() {
        super(Map.class);
    }
}
