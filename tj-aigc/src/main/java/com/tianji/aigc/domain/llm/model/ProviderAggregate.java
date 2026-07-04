package com.tianji.aigc.domain.llm.model;

import com.tianji.aigc.domain.llm.model.config.ProviderConfig;
import com.tianji.aigc.infrastructure.llm.protocol.enums.ProviderProtocol;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProviderAggregate {

    private ProviderEntity entity;
    private List<ModelEntity> models = new ArrayList<>();

    public ProviderAggregate(ProviderEntity entity, List<ModelEntity> models) {
        this.entity = entity;
        if (models != null) {
            this.models = models;
        }
    }

    public void addModel(ModelEntity model) {
        if (model != null && model.getProviderId().equals(entity.getId())) {
            models.add(model);
        }
    }

    public void setModels(List<ModelEntity> models) {
        this.models = models != null ? models : new ArrayList<>();
    }

    public List<ModelEntity> getModels() {
        return models;
    }

    public ProviderConfig getConfig() {
        return entity.getConfig();
    }

    public void setConfig(ProviderConfig config) {
        entity.setConfig(config);
    }

    public String getId() {
        return entity.getId();
    }

    public String getUserId() {
        return entity.getUserId();
    }

    public ProviderProtocol getProtocol() {
        return entity.getProtocol();
    }

    public void setProtocol(ProviderProtocol code) {
        entity.setProtocol(code);
    }

    public String getName() {
        return entity.getName();
    }

    public void setName(String name) {
        entity.setName(name);
    }

    public String getDescription() {
        return entity.getDescription();
    }

    public void setDescription(String description) {
        entity.setDescription(description);
    }

    public Boolean getIsOfficial() {
        return entity.getIsOfficial();
    }

    public void setIsOfficial(Boolean isOfficial) {
        entity.setIsOfficial(isOfficial);
    }

    public Boolean getStatus() {
        return entity.getStatus();
    }

    public void setStatus(Boolean status) {
        entity.setStatus(status);
    }

    public LocalDateTime getCreatedAt() {
        return entity.getCreatedAt();
    }

    public LocalDateTime getUpdatedAt() {
        return entity.getUpdatedAt();
    }

    public LocalDateTime getDeletedAt() {
        return entity.getDeletedAt();
    }

    public ProviderEntity getEntity() {
        return entity;
    }
}
