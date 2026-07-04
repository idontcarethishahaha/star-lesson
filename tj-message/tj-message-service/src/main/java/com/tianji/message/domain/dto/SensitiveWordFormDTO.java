package com.tianji.message.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "敏感词表单实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWordFormDTO {
    @Schema(description = "主键id，新增时无需填写")
    private Long id;
    @Schema(description = "敏感词")
    private String sensitives;
}