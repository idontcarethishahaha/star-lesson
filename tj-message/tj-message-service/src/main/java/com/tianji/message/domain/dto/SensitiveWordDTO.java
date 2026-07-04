package com.tianji.message.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "敏感词实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWordDTO {
    @Schema(description = "主键id")
    private Long id;
    @Schema(description = "敏感词")
    private String sensitives;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}