package com.tianji.aigc.domain.llm.event;

import com.tianji.aigc.domain.llm.model.ModelEntity;

public class ModelUpdatedEvent extends ModelDomainEvent {

    private final ModelEntity model;

    public ModelUpdatedEvent(String modelId, String userId, ModelEntity model) {
        super(modelId, userId);
        this.model = model;
    }

    public ModelEntity getModel() {
        return model;
    }
}
