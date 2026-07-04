package com.tianji.chat.domain.query;

import com.tianji.common.domain.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程评价查询对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "历史记录查询条件")
public class RecordQuery extends PageQuery {

    @Schema(description = "会话ID")
    private String sessionId;

}    