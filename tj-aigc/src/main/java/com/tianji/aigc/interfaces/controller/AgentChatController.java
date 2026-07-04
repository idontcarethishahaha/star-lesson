package com.tianji.aigc.interfaces.controller;

import com.tianji.aigc.application.chat.dto.AgentChatDTO;
import com.tianji.aigc.application.chat.service.AgentChatService;
import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.common.annotations.NoWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/agent-chat")
@RequiredArgsConstructor
public class AgentChatController {

    private final AgentChatService agentChatService;

    @NoWrapper
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEventVO> chat(@RequestBody AgentChatDTO chatDTO) {
        return this.agentChatService.chat(chatDTO);
    }

    @PostMapping("/stop")
    public void stop(@RequestParam("sessionId") String sessionId) {
        this.agentChatService.stop(sessionId);
    }

    @PostMapping("/text")
    public String chatText(@RequestParam String question, @RequestParam(required = false) String agentId) {
        return this.agentChatService.chatText(question, agentId);
    }

    @GetMapping("/welcome")
    public String getWelcomeMessage(@RequestParam String agentId) {
        return agentChatService.getWelcomeMessage(agentId);
    }
}
