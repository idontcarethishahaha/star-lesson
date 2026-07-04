package com.tianji.data.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TodoVO {
    @Schema(description = "退款待审批数量")
    private Integer todoRefundNum;
    @Schema(description = "优惠券待发布数量")
    private Integer todoCouponNum;
}
