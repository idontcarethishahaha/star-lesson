package com.tianji.aigc.domain.rag.strategy.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.tianji.aigc.domain.rag.strategy.DocumentProcessingStrategy;

@Service(value = "markdown")
public class MarkdownRagDocumentProcessing implements DocumentProcessingStrategy {

    private static final Logger log = LoggerFactory.getLogger(MarkdownRagDocumentProcessing.class);

    @Override
    public void handle(String fileId, String strategy) throws Exception {
        log.info("开始Markdown文档处理 文件: {}", fileId);

        log.info("完成Markdown文档处理 文件: {}", fileId);
    }
}
