package com.tianji.aigc.domain.tool.constant;

import com.tianji.aigc.infrastructure.exception.BusinessException;

public enum ToolType {

    MCP,
    BUILTIN;

    public static ToolType fromCode(String code) {
        for (ToolType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new BusinessException("未知的工具类型码: " + code);
    }
}
