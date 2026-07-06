package com.tianji.aigc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tj.ai.prompt")
public class AIProperties {

    private System system;
    private Rag rag;

    @Data
    public static class System {
        private Chat chat;
        private Chat routeAgent;
        private Chat recommendAgent;
        private Chat consultAgent;
        private Chat buyAgent;
        private Chat text;

        @Data
        public static class Chat {
            private String dataId;
            private String group = "sl-group";
            private long timeoutMs = 20000L;
        }
    }

    @Data
    public static class Rag {
        private double similarityThreshold = 0.6;
        private int topK = 6;
        private List<String> recommendKnowledgeBaseIds;
        private List<String> consultKnowledgeBaseIds;
    }
}
