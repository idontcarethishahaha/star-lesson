package com.tianji.aigc.domain.llm.event;

import java.time.LocalDateTime;

public abstract class ModelDomainEvent {

    private final String modelId;

    private final String userId;

    private final LocalDateTime occurredAt;

    public ModelDomainEvent(String modelId, String userId) {
        this.modelId = modelId;
        this.userId = userId;
        this.occurredAt = LocalDateTime.now();
    }

    public String getModelId() {
        return modelId;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
