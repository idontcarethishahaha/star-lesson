package com.tianji.data.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UrlLogVO {
    @Schema(description = "请求ID")
    private String requestId;
    @Schema(description = "创建时间")
    private String time;
    @Schema(description = "请求路径")
    private String requestUri;
    @Schema(description = "请求方式")
    private String requestMethod;
    @Schema(description = "请求主机")
    private String host;
    @Schema(description = "主机地址")
    private String hostAddress;
    @Schema(description = "响应时间(ms)")
    private Long responseTime;
    @Schema(description = "响应状态")
    private String responseCode;
    @Schema(description = "请求body")
    private String requestBody;
    @Schema(description = "应答body")
    private String responseBody;
    @Schema(description = "应答msg")
    private String responseMsg;
    @Schema(description = "用户id")
    private String userId;
    @Schema(description = "用户名称")
    private String userName;
    @Schema(description = "业务类型")
    private String businessType;
    @Schema(description = "数据标志位")
    private String dataState;
    @Schema(description = "省")
    private String province;
    @Schema(description = "市")
    private String city;
}
