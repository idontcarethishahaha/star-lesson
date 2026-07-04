package com.tianji.chat.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

import static com.tianji.chat.constants.AiConstants.QDRANT_COLLECTION;

@Data
@Configuration
@Slf4j
public class QdrantConfig {

    @Value("${qdrant.host:192.168.227.128}")
    private String host;

    @Value("${qdrant.port:6334}")
    private int port;

    @Value("${qdrant.secure:false}")
    private boolean secure;

    @PostConstruct
    public void debugPrint() {
        log.info("[QdrantConfig] host={}, port={}, secure={}", host, port, secure);
    }

    @Bean
    public QdrantClient qdrantClient() {
        if (host == null || host.isBlank()) {
            throw new IllegalStateException(
                    "Qdrant host 未配置（qdrant.host 为空）。请在 bootstrap.yml / shared-qdrant.yaml 中设置 qdrant.host、qdrant.port、qdrant.secure，或通过 Nacos 加载 shared-qdrant.yaml。");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalStateException(
                    "Qdrant port 非法：" + port + "。请设置 qdrant.port 为 1-65535。");
        }
        QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder(host, port, secure);
        return new QdrantClient(grpcClientBuilder.build());
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("Qdrant host 未配置（qdrant.host 为空），无法构建 EmbeddingStore。");
        }
        return QdrantEmbeddingStore.builder()
                .host(host)
                .port(port)
                .collectionName(QDRANT_COLLECTION)
                .build();
    }
}
