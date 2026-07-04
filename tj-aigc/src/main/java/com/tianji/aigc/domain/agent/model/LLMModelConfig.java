package com.tianji.aigc.domain.agent.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LLMModelConfig {

    private String modelId;

    private Double temperature = 0.7;

    private Double topP = 0.7;

    private Integer topK = 50;

    private Integer maxTokens;

    private TokenOverflowStrategyEnum strategyType = TokenOverflowStrategyEnum.NONE;

    private Double reserveRatio;

    private Integer summaryThreshold;

    public LLMModelConfig() {
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public TokenOverflowStrategyEnum getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(TokenOverflowStrategyEnum strategyType) {
        this.strategyType = strategyType;
    }

    public Double getReserveRatio() {
        return reserveRatio;
    }

    public void setReserveRatio(Double reserveRatio) {
        this.reserveRatio = reserveRatio;
    }

    public Integer getSummaryThreshold() {
        return summaryThreshold;
    }

    public void setSummaryThreshold(Integer summaryThreshold) {
        this.summaryThreshold = summaryThreshold;
    }
}
