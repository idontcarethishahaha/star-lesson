package com.tianji.message.domain.query;

import com.tianji.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "敏感词查询对象")
@Data
public class SensitiveWordPageQuery extends PageQuery {
    @Schema(description = "敏感词关键字")
    private String keyword;
}