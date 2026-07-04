package com.tianji.aigc.application.rag.service;

import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.constants.AiConstants;
import com.tianji.aigc.domain.rag.model.DocumentUnitEntity;
import com.tianji.aigc.domain.rag.model.FileDetailEntity;
import com.tianji.aigc.infrastructure.exception.BusinessException;
import com.tianji.aigc.mapper.DocumentUnitMapper;
import com.tianji.aigc.mapper.FileDetailMapper;
import com.tianji.aigc.utils.QdrantEmbeddingUtils;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.WithVectorsSelectorFactory;
import io.qdrant.client.grpc.Points;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagVectorService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final QdrantClient qdrantClient;
    private final FileDetailMapper fileDetailMapper;
    private final DocumentUnitMapper documentUnitMapper;

    private static final int MAX_CHUNK_SIZE = 800;
    private static final int OVERLAP_SIZE = 100;
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.6;
    private static final int DEFAULT_TOP_K = 6;

    @Transactional
    public void vectorizeFile(String fileId) {
        FileDetailEntity file = fileDetailMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }

        file.setProcessingStatus(3);
        fileDetailMapper.updateById(file);

        try {
            List<DocumentUnitEntity> documents = listDocumentsByFileId(fileId);
            if (documents.isEmpty()) {
                file.setProcessingStatus(6);
                fileDetailMapper.updateById(file);
                return;
            }

            int totalPages = documents.size();
            int currentPage = 0;

            for (DocumentUnitEntity doc : documents) {
                currentPage++;
                try {
                    if (Boolean.TRUE.equals(doc.getIsVector()) && StrUtil.isNotBlank(doc.getVectorId())) {
                        continue;
                    }

                    String content = doc.getContent();
                    if (StrUtil.isBlank(content)) {
                        continue;
                    }

                    List<String> chunks = splitText(content, MAX_CHUNK_SIZE, OVERLAP_SIZE);
                    List<DocumentUnitEntity> chunkDocs = new ArrayList<>();

                    for (int i = 0; i < chunks.size(); i++) {
                        String chunk = chunks.get(i);
                        String vectorId = UUID.randomUUID().toString();

                        TextSegment segment = TextSegment.from(chunk);
                        segment.metadata().put("dataset_id", file.getDataSetId());
                        segment.metadata().put("file_id", fileId);
                        segment.metadata().put("page", String.valueOf(doc.getPage()));
                        segment.metadata().put("chunk_index", String.valueOf(i));

                        Embedding embedding = embeddingModel.embed(segment).content();
                        embeddingStore.add(embedding, segment);

                        DocumentUnitEntity chunkDoc;
                        if (i == 0) {
                            chunkDoc = doc;
                        } else {
                            chunkDoc = new DocumentUnitEntity();
                            chunkDoc.setId(generateId());
                            chunkDoc.setFileId(fileId);
                            chunkDoc.setPage(doc.getPage());
                            chunkDoc.setIsOcr(true);
                        }
                        chunkDoc.setContent(chunk);
                        chunkDoc.setIsVector(true);
                        chunkDoc.setVectorId(vectorId);

                        chunkDocs.add(chunkDoc);
                    }

                    for (DocumentUnitEntity chunkDoc : chunkDocs) {
                        if (chunkDoc.getId() != null && documentUnitMapper.selectById(chunkDoc.getId()) != null) {
                            documentUnitMapper.updateById(chunkDoc);
                        } else {
                            documentUnitMapper.insert(chunkDoc);
                        }
                    }

                    file.setCurrentEmbeddingPageNumber(currentPage);
                    file.setEmbeddingProcessProgress(totalPages > 0 ? (double) currentPage / totalPages * 100 : 0);
                    fileDetailMapper.updateById(file);

                } catch (Exception e) {
                    log.error("文档向量化失败, fileId={}, docId={}", fileId, doc.getId(), e);
                }
            }

            file.setProcessingStatus(4);
            file.setEmbeddingProcessProgress(100.0);
            fileDetailMapper.updateById(file);

        } catch (Exception e) {
            log.error("文件向量化失败, fileId={}", fileId, e);
            file.setProcessingStatus(6);
            fileDetailMapper.updateById(file);
        }
    }

    public List<DocumentUnitEntity> search(List<String> datasetIds, String query, Double threshold, Integer topK) {
        if (StrUtil.isBlank(query)) {
            return new ArrayList<>();
        }

        double similarityThreshold = threshold != null ? threshold : DEFAULT_SIMILARITY_THRESHOLD;
        int limit = topK != null ? topK : DEFAULT_TOP_K;

        try {
            Embedding queryEmbedding = embeddingModel.embed(query).content();

            Points.Filter.Builder filterBuilder = Points.Filter.newBuilder();
            if (datasetIds != null && !datasetIds.isEmpty()) {
                for (String datasetId : datasetIds) {
                    filterBuilder.addShould(matchKeyword("dataset_id", datasetId));
                }
            }

            List<Points.ScoredPoint> results = qdrantClient.searchAsync(Points.SearchPoints.newBuilder()
                    .setCollectionName(AiConstants.QDRANT_COLLECTION)
                    .addAllVector(queryEmbedding.vectorAsList())
                    .setLimit(limit)
                    .setWithPayload(enable(true))
                    .setWithVectors(WithVectorsSelectorFactory.enable(true))
                    .setFilter(filterBuilder.build())
                    .build()).get();

            List<EmbeddingMatch<TextSegment>> matches = results.stream()
                    .map(point -> QdrantEmbeddingUtils.toEmbeddingMatch(point, queryEmbedding))
                    .toList();

            List<DocumentUnitEntity> docs = new ArrayList<>();
            for (EmbeddingMatch<TextSegment> match : matches) {
                if (match.score() < similarityThreshold) {
                    continue;
                }
                DocumentUnitEntity doc = new DocumentUnitEntity();
                doc.setId(match.embeddingId());
                doc.setVectorId(match.embeddingId());
                doc.setIsVector(true);
                doc.setSimilarityScore(match.score());

                TextSegment segment = match.embedded();
                if (segment != null) {
                    doc.setContent(segment.text());

                    String fileId = segment.metadata().get("file_id");
                    if (fileId != null) {
                        doc.setFileId(fileId);
                    }

                    String pageStr = segment.metadata().get("page");
                    if (pageStr != null) {
                        try {
                            doc.setPage(Integer.parseInt(pageStr));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }

                docs.add(doc);
            }

            return docs;

        } catch (InterruptedException | ExecutionException e) {
            log.error("向量搜索失败, query={}", query, e);
            Thread.currentThread().interrupt();
            return new ArrayList<>();
        }
    }

    public void deleteVectorsByFileId(String fileId) {
        try {
            Points.Filter filter = Points.Filter.newBuilder()
                    .addMust(matchKeyword("file_id", fileId))
                    .build();

            qdrantClient.deleteAsync(AiConstants.QDRANT_COLLECTION, filter).get();
            log.info("已删除文件的向量数据, fileId={}", fileId);
        } catch (InterruptedException | ExecutionException e) {
            log.error("删除向量数据失败, fileId={}", fileId, e);
            Thread.currentThread().interrupt();
        }
    }

    public void deleteVectorsByDatasetId(String datasetId) {
        try {
            Points.Filter filter = Points.Filter.newBuilder()
                    .addMust(matchKeyword("dataset_id", datasetId))
                    .build();

            qdrantClient.deleteAsync(AiConstants.QDRANT_COLLECTION, filter).get();
            log.info("已删除数据集的向量数据, datasetId={}", datasetId);
        } catch (InterruptedException | ExecutionException e) {
            log.error("删除向量数据失败, datasetId={}", datasetId, e);
            Thread.currentThread().interrupt();
        }
    }

    private List<DocumentUnitEntity> listDocumentsByFileId(String fileId) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DocumentUnitEntity>();
        wrapper.eq(DocumentUnitEntity::getFileId, fileId);
        wrapper.isNull(DocumentUnitEntity::getDeletedAt);
        wrapper.orderByAsc(DocumentUnitEntity::getPage);
        return documentUnitMapper.selectList(wrapper);
    }

    private List<String> splitText(String text, int maxChunkSize, int overlapSize) {
        List<String> chunks = new ArrayList<>();
        if (StrUtil.isBlank(text)) {
            return chunks;
        }

        text = text.trim();
        int length = text.length();

        if (length <= maxChunkSize) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;
        while (start < length) {
            int end = Math.min(start + maxChunkSize, length);

            if (end < length) {
                int lastPunctuation = findLastPunctuation(text, start, end);
                if (lastPunctuation > start) {
                    end = lastPunctuation + 1;
                }
            }

            chunks.add(text.substring(start, end).trim());

            if (end >= length) {
                break;
            }

            start = end - overlapSize;
            if (start < 0) {
                start = 0;
            }
        }

        return chunks;
    }

    private int findLastPunctuation(String text, int start, int end) {
        String punctuations = "。！？.!?\n；;";
        for (int i = end - 1; i >= start; i--) {
            if (punctuations.indexOf(text.charAt(i)) != -1) {
                return i;
            }
        }
        return -1;
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}