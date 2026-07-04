package com.tianji.data.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OrderBoardVO {
    @Schema(description = "总订单金额")
    private String allOrderAmount;
    @Schema(description = "待支付金额")
    private String noPayAmount;
    @Schema(description = "已关闭金额")
    private String closeAmount;
    @Schema(description = "退款金额")
    private String refundAmount;
    @Schema(description = "实付金额")
    private String realAmount;
}
