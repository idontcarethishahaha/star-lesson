package com.tianji.aigc.application.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatDTO {

    private String question;

    private String sessionId;

    private String agentId;

    private String conversationId;
}
