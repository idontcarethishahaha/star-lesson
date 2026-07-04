package com.tianji.aigc.domain.rag.constant;

public enum SearchType {

    VECTOR("vector", "向量检索"),

    KEYWORD("keyword", "关键词检索"),

    HYBRID("hybrid", "混合检索");

    private final String code;
    private final String description;

    SearchType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static SearchType fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (SearchType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return code;
    }
}
