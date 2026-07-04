package com.tianji.aigc.application.rag.dto;

import lombok.Data;

@Data
public class RagVersionCreateRequest {

    private String ragId;

    private String version;

    private String changeLog;
}