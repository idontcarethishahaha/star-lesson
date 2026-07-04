package com.tianji.aigc.config;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LangChain4j EmbeddingStore 适配到 Spring AI VectorStore
 *
 * @author lusy
 */
@Slf4j
@RequiredArgsConstructor
public class LangChain4jEmbeddingStoreAdapter implements VectorStore {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    @Override
    public void add(List<Document> documents) {
        for (Document doc : documents) {
            TextSegment segment = TextSegment.from(doc.getText(), toLangChain4jMetadata(doc.getMetadata()));
            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(embedding, segment);
        }
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        Embedding queryEmbedding = embeddingModel.embed(request.getQuery()).content();
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(request.getTopK())
                .minScore(request.getSimilarityThreshold())
                .build();
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(searchRequest);
        return result.matches().stream()
                .map(this::toSpringAIDocument)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(List<String> ids) {
        log.warn("delete(List<String>) 调用但 LangChain4j EmbeddingStore 不支持按ID删除，已忽略");
    }

    @Override
    public void delete(Filter.Expression filterExpression) {
        log.warn("delete(Filter.Expression) 调用但 LangChain4j EmbeddingStore 不支持按表达式删除，已忽略");
    }

    private Document toSpringAIDocument(EmbeddingMatch<TextSegment> match) {
        TextSegment segment = match.embedded();
        if (segment == null) {
            return new Document(String.valueOf(match.score()), "", Map.of());
        }
        return new Document(
                match.embeddingId(),
                segment.text(),
                toSpringAIMetadata(segment.metadata())
        );
    }

    private dev.langchain4j.data.document.Metadata toLangChain4jMetadata(Map<String, Object> metadata) {
        dev.langchain4j.data.document.Metadata result = new dev.langchain4j.data.document.Metadata();
        if (metadata != null) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    result.put(entry.getKey(), entry.getValue().toString());
                }
            }
        }
        return result;
    }

    private Map<String, Object> toSpringAIMetadata(dev.langchain4j.data.document.Metadata metadata) {
        return metadata.asMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
