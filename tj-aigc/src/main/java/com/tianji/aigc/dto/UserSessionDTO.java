package com.tianji.aigc.dto;

import lombok.Data;

@Data
public class UserSessionDTO {
    private String name;
    private String tag;
    private Long userId;
}