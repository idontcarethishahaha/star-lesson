package com.tianji.aigc.domain.llm.model;

import com.baomidou.mybatisplus.annotation.*;
import com.tianji.aigc.domain.llm.model.config.ProviderConfig;
import com.tianji.aigc.infrastructure.converter.ProviderConfigConverter;
import com.tianji.aigc.infrastructure.converter.ProviderProtocolConverter;
import com.tianji.aigc.infrastructure.entity.BaseEntity;
import com.tianji.aigc.infrastructure.exception.BusinessException;
import com.tianji.aigc.infrastructure.llm.protocol.enums.ProviderProtocol;

import java.util.Objects;

@TableName("providers")
public class ProviderEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    @TableField(typeHandler = ProviderProtocolConverter.class)
    private ProviderProtocol protocol;
    private String name;
    private String description;

    @TableField(typeHandler = ProviderConfigConverter.class)
    private ProviderConfig config;

    private Boolean isOfficial;
    private Boolean status;

    public void setConfig(ProviderConfig config) {
        this.config = config;
    }

    public ProviderConfig getConfig() {
        return config;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ProviderProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(ProviderProtocol protocol) {
        this.protocol = protocol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsOfficial() {
        return isOfficial;
    }

    public void setIsOfficial(Boolean official) {
        isOfficial = official;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public void isActive() {
        if (!status) {
            throw new BusinessException("服务商未激活");
        }
    }

    public void isAvailable(String userId) {
        if (!isOfficial && !Objects.equals(this.getUserId(), userId)) {
            throw new BusinessException("模型未找到");
        }
    }
}
