package com.tianji.aigc.application.rag.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentUnitDTO {

    private String id;

    private String fileId;

    private Integer page;

    private String content;

    private Boolean isVector;

    private Boolean isOcr;

    private String vectorId;

    private LocalDateTime createdAt;
}