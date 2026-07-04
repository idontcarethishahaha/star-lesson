package com.tianji.aigc.infrastructure.converter;

import org.apache.ibatis.type.MappedTypes;

import java.util.List;

@MappedTypes(List.class)
public class ListStringConverter extends JsonToStringConverter<List<String>> {

    public ListStringConverter() {
        super((Class<List<String>>) (Class<?>) List.class);
    }
}
