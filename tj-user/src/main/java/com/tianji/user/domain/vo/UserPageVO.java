package com.tianji.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页用户信息")
public class UserPageVO {
    @Schema(description = "用户id", example = "1")
    private Long id;

    @Schema(description = "用户名称", example = "张三")
    private String name;

    @Schema(description = "头像", example = "default-user-icon.jpg")
    private String icon;

    @Schema(description = "手机号", example = "13800010004")
    private String cellPhone;

    @Schema(description = "用户类型：1-其他员工, 2-普通学员，3-老师", example = "1")
    private Integer type;

    @Schema(description = "注册时间", example = "2022-07-12")
    private LocalDateTime createTime;

    @Schema(description = "账户状态，0-禁用，1-正常", example = "1")
    private Integer status;
}
