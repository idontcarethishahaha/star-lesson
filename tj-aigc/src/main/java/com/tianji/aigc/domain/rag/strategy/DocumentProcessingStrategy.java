package com.tianji.aigc.domain.rag.strategy;

public interface DocumentProcessingStrategy {

    void handle(String fileId, String strategy) throws Exception;

}
