package com.tianji.aigc.domain.llm.model.enums;

public enum ProviderType {

    ALL("all"),

    OFFICIAL("official"),

    CUSTOM("custom");

    private final String code;

    ProviderType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ProviderType fromCode(String code) {
        for (ProviderType type : ProviderType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return ProviderType.ALL;
    }
}
