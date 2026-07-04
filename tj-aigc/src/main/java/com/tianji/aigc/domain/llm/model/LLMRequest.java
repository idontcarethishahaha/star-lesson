package com.tianji.aigc.domain.llm.model;

import java.util.List;

public class LLMRequest {
    private final List<LLMMessage> messages;
    private final LLMRequestParameters parameters;

    public LLMRequest(List<LLMMessage> messages, LLMRequestParameters parameters) {
        this.messages = messages;
        this.parameters = parameters;
    }

    public List<LLMMessage> getMessages() {
        return messages;
    }

    public LLMRequestParameters getParameters() {
        return parameters;
    }

    public enum MessageType {
        USER, SYSTEM, ASSISTANT
    }

    public static class LLMMessage {
        private final MessageType type;
        private final String content;

        public LLMMessage(MessageType type, String content) {
            this.type = type;
            this.content = content;
        }

        public MessageType getType() {
            return type;
        }

        public String getContent() {
            return content;
        }
    }

    public static class LLMRequestParameters {
        private final String modelId;
        private final Double temperature;
        private final Double topP;

        public LLMRequestParameters(String modelId, Double temperature, Double topP) {
            this.modelId = modelId;
            this.temperature = temperature;
            this.topP = topP;
        }

        public String getModelId() {
            return modelId;
        }

        public Double getTemperature() {
            return temperature;
        }

        public Double getTopP() {
            return topP;
        }
    }

    public static class Builder {
        private List<LLMMessage> messages;
        private LLMRequestParameters parameters;

        public Builder messages(List<LLMMessage> messages) {
            this.messages = messages;
            return this;
        }

        public Builder parameters(LLMRequestParameters parameters) {
            this.parameters = parameters;
            return this;
        }

        public LLMRequest build() {
            return new LLMRequest(messages, parameters);
        }
    }
}
