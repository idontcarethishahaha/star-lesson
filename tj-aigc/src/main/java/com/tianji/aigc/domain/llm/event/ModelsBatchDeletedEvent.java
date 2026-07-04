package com.tianji.aigc.domain.llm.event;

import java.time.LocalDateTime;
import java.util.List;

public class ModelsBatchDeletedEvent {

    private final List<ModelDeleteItem> modelDeleteItems;
    private final String userId;
    private final LocalDateTime occurredAt;

    public ModelsBatchDeletedEvent(List<ModelDeleteItem> modelDeleteItems, String userId) {
        this.modelDeleteItems = modelDeleteItems;
        this.userId = userId;
        this.occurredAt = LocalDateTime.now();
    }

    public List<ModelDeleteItem> getModelDeleteItems() {
        return modelDeleteItems;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public static class ModelDeleteItem {
        private final String modelId;
        private final String userId;

        public ModelDeleteItem(String modelId, String userId) {
            this.modelId = modelId;
            this.userId = userId;
        }

        public String getModelId() {
            return modelId;
        }

        public String getUserId() {
            return userId;
        }
    }
}
