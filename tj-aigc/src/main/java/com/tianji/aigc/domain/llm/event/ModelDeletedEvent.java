package com.tianji.aigc.domain.llm.event;

public class ModelDeletedEvent extends ModelDomainEvent {

    public ModelDeletedEvent(String modelId, String userId) {
        super(modelId, userId);
    }
}
