package com.tianji.aigc.config;

import com.tianji.aigc.constants.AiConstants;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class QdrantConfig {

    private final QdrantProperties qdrantProperties;

    @PostConstruct
    public void debugPrint() {
        log.info("[QdrantConfig] host={}, httpPort={}, grpcPort={}, secure={}",
                qdrantProperties.getHost(), qdrantProperties.getPort(),
                qdrantProperties.getGrpcPort(), qdrantProperties.isSecure());
    }

    @Bean
    public QdrantClient qdrantClient() {
        QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder(
                qdrantProperties.getHost(),
                qdrantProperties.getGrpcPort(),
                qdrantProperties.isSecure()
        );
        return new QdrantClient(grpcClientBuilder.build());
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return QdrantEmbeddingStore.builder()
                .host(qdrantProperties.getHost())
                .port(qdrantProperties.getPort())
                .collectionName(AiConstants.QDRANT_COLLECTION)
                .build();
    }
}
