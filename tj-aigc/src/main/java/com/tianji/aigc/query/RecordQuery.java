package com.tianji.aigc.query;

import com.tianji.common.domain.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RecordQuery extends PageQuery {
    private String sessionId;
}