package com.tianji.aigc.utils;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.CosineSimilarity;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.RelevanceScore;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;

import java.util.Map;
import java.util.stream.Collectors;

public class QdrantEmbeddingUtils {

    private static final String TEXT_SEGMENT_KEY = "text_segment";

    public static EmbeddingMatch<TextSegment> toEmbeddingMatch(
            Points.ScoredPoint scoredPoint,
            Embedding referenceEmbedding
    ) {
        Map<String, JsonWithInt.Value> payload = scoredPoint.getPayloadMap();
        JsonWithInt.Value textSegmentValue = payload.getOrDefault(TEXT_SEGMENT_KEY, null);

        Map<String, String> metadata = payload.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(TEXT_SEGMENT_KEY))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getStringValue()
                ));

        Embedding embedding = Embedding.from(scoredPoint.getVectors().getVector().getDataList());
        double cosineSimilarity = CosineSimilarity.between(embedding, referenceEmbedding);

        return new EmbeddingMatch<>(
                RelevanceScore.fromCosineSimilarity(cosineSimilarity),
                scoredPoint.getId().getUuid(),
                embedding,
                textSegmentValue == null
                        ? null
                        : TextSegment.from(textSegmentValue.getStringValue(), new Metadata(metadata))
        );
    }
}
