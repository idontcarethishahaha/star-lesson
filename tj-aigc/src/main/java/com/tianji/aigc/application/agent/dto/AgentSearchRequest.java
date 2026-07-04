package com.tianji.aigc.application.agent.dto;

import lombok.Data;

@Data
public class AgentSearchRequest {

    private Integer pageNo = 1;

    private Integer pageSize = 10;

    private String name;

    private String userId;

    private Boolean enabled;
}
