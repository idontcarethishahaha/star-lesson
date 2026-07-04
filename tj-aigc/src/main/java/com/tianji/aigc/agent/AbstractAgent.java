package com.tianji.aigc.agent;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.config.ToolResultHolder;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.common.utils.UserContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
public abstract class AbstractAgent implements Agent {

    public static final ChatEventVO STOP_EVENT = ChatEventVO.builder().eventType(ChatEventTypeEnum.STOP.getValue()).build();

    @Resource
    private ChatClient chatClient;
    @Resource
    private ChatMemory chatMemory;
    @Resource
    private ChatSessionService chatSessionService;

    /**
     * 使用 ObjectProvider 避免 StringRedisTemplate 不存在时导致 Bean 创建失败
     */
    @Autowired
    private ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider;

    private static final String GENERATE_STATUS_KEY = "GENERATE_STATUS";

    /**
     * 安全获取 StringRedisTemplate（可能为 null）
     */
    private StringRedisTemplate getStringRedisTemplate() {
        return this.stringRedisTemplateProvider.getIfAvailable();
    }

    @Override
    public Flux<ChatEventVO> processStream(String question, String sessionId) {
        // 生成请求id
        var requestId = this.generateRequestId();
        StringRedisTemplate redisTemplate = this.getStringRedisTemplate();
        // 将会话id转化为对话id
        var conversationId = ChatService.getConversationId(sessionId);
        // 大模型输出内容的缓存器，用于在输出中断后的数据存储
        var outputBuilder = new StringBuilder();
        // 获取到当前登录的用户id
        var userId = UserContext.getUser();
        //更新会话时间
        this.chatSessionService.update(sessionId, question, userId);

        return this.getChatClientRequest(question, sessionId, requestId)
                .stream()
                .chatResponse()
                .doFirst(() -> {
                    if (redisTemplate != null) {
                        var hashOps = redisTemplate.boundHashOps(GENERATE_STATUS_KEY);
                        hashOps.put(sessionId, "true");
                    }
                })
                .doOnError(throwable -> {
                    if (redisTemplate != null) {
                        redisTemplate.boundHashOps(GENERATE_STATUS_KEY).delete(sessionId);
                    }
                })
                .doOnComplete(() -> {
                    if (redisTemplate != null) {
                        redisTemplate.boundHashOps(GENERATE_STATUS_KEY).delete(sessionId);
                    }
                })
                .doOnCancel(() -> {
                    // 当输出被取消时，保存输出的内容到历史记录中
                    this.saveStopHistoryRecord(conversationId, outputBuilder.toString());
                })
                .takeWhile(response -> {
                    if (redisTemplate == null) {
                        return true; // 没有 redis 时不阻断流
                    }
                    return redisTemplate.boundHashOps(GENERATE_STATUS_KEY).get(sessionId) != null;
                })
                .map(chatResponse -> {
                    // 大模型生成的内容
                    var text = chatResponse.getResult().getOutput().getText();
                    // 追加到输出内容
                    outputBuilder.append(text);

                    // 获取到消息的结束原因
                    var finishReason = chatResponse.getResult().getMetadata().getFinishReason();
                    if (StrUtil.equals(finishReason, Constant.STOP)) {
                        // 获取到消息id
                        var messageId = chatResponse.getMetadata().getId();
                        // 将消息id与请求id进行关联
                        ToolResultHolder.put(messageId, Constant.REQUEST_ID, requestId);
                    }

                    return ChatEventVO.builder()
                            .eventData(text)
                            .eventType(ChatEventTypeEnum.DATA.getValue())
                            .build();
                })
                .concatWith(Flux.defer(() -> {
                    var result = ToolResultHolder.get(requestId);
                    if (ObjectUtil.isNotEmpty(result)) {
                        ToolResultHolder.remove(requestId);
                        // 工具被调用了，需要向前端传递参数
                        return Flux.just(ChatEventVO.builder()
                                .eventType(ChatEventTypeEnum.PARAM.getValue())
                                .eventData(result)
                                .build(), STOP_EVENT);
                    }
                    return Flux.just(STOP_EVENT); // 结束标识
                }));
    }

    @Override
    public String process(String question, String sessionId) {
        // 生成请求id
        var requestId = this.generateRequestId();
        // 获取到当前登录的用户id
        var userId = UserContext.getUser();
        //更新会话时间
        this.chatSessionService.update(sessionId, question, userId);

        return this.getChatClientRequest(question, sessionId, requestId)
                .call()
                .content();
    }

    private ChatClient.ChatClientRequestSpec getChatClientRequest(String question, String sessionId, String requestId) {
        return this.chatClient.prompt()
                .system(promptSystemSpec -> promptSystemSpec.text(this.systemMessage()).params(this.systemMessageParams()))
                .advisors(advisorSpec -> advisorSpec.advisors(this.advisors()).params(this.advisorParams(sessionId, requestId)))
                .tools(this.tools())
                .toolContext(this.toolContext(sessionId, requestId))
                .user(question);
    }

    /**
     * 保存停止输出的记录
     *
     * @param conversationId 会话id
     * @param content        大模型输出的内容
     */
    private void saveStopHistoryRecord(String conversationId, String content) {
        this.chatMemory.add(conversationId, new AssistantMessage(content));
    }

    private String generateRequestId() {
        return IdUtil.fastSimpleUUID();
    }

    @Override
    public void stop(String sessionId) {
        StringRedisTemplate redisTemplate = this.getStringRedisTemplate();
        if (redisTemplate != null) {
            var hashOps = redisTemplate.boundHashOps(GENERATE_STATUS_KEY);
            hashOps.delete(sessionId);
        }
    }

    @Override
    public Map<String, Object> advisorParams(String sessionId, String requestId) {
        // 将会话id转化为对话id
        var conversationId = ChatService.getConversationId(sessionId);
        return Map.of(ChatMemory.CONVERSATION_ID, conversationId);
    }
}
