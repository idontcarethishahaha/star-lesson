package com.tianji.aigc.infrastructure.converter;

import org.apache.ibatis.type.MappedTypes;

import java.util.ArrayList;
import java.util.List;

@MappedTypes(ArrayList.class)
public class ListConverter extends JsonToStringConverter<ArrayList> {

    public ListConverter() {
        super(ArrayList.class);
    }
}
