package com.tianji.aigc.infrastructure.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String toJsonString(Object obj) {
        if (obj == null) {
            return "{}";
        }

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON序列化失败: {}, 错误: {}", obj.getClass().getSimpleName(), e.getMessage(), e);
            return "{}";
        }
    }

    public static <T> T parseObject(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("JSON反序列化失败: {}, 错误: {}", clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("JSON数组反序列化失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public static Map<String, Object> parseMap(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.error("JSON Map反序列化失败: {}", e.getMessage(), e);
            return null;
        }
    }

    public static String toJson(Object obj) {
        return toJsonString(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return parseObject(json, clazz);
    }

    public static <T> List<T> fromList(String json, Class<T> clazz) {
        return parseArray(json, clazz);
    }
}
