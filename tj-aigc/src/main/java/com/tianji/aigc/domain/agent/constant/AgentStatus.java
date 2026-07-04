package com.tianji.aigc.domain.agent.constant;

import com.tianji.aigc.infrastructure.exception.BusinessException;

public enum AgentStatus {

    DRAFT(0, "草稿"),

    PENDING_REVIEW(1, "待审核"),

    PUBLISHED(2, "已上架"),

    UNPUBLISHED(3, "已下架"),

    REJECTED(4, "审核拒绝");

    private final Integer code;
    private final String description;

    AgentStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AgentStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (AgentStatus status : AgentStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }

        throw new BusinessException("INVALID_AGENT_STATUS", "无效的Agent状态码: " + code);
    }
}
