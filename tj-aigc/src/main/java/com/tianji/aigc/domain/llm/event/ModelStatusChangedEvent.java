package com.tianji.aigc.domain.llm.event;

import com.tianji.aigc.domain.llm.model.ModelEntity;

public class ModelStatusChangedEvent extends ModelDomainEvent {

    private final ModelEntity model;
    private final boolean newStatus;
    private final String reason;

    public ModelStatusChangedEvent(String modelId, String userId, ModelEntity model, boolean newStatus, String reason) {
        super(modelId, userId);
        this.model = model;
        this.newStatus = newStatus;
        this.reason = reason;
    }

    public ModelEntity getModel() {
        return model;
    }

    public boolean isNewStatus() {
        return newStatus;
    }

    public String getReason() {
        return reason;
    }
}
