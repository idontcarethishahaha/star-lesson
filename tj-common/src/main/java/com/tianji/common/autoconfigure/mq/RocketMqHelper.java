package com.tianji.common.autoconfigure.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class RocketMqHelper {

    private final RocketMQTemplate rocketMQTemplate;
    private final ThreadPoolTaskExecutor executor;

    public RocketMqHelper(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(99999);
        executor.setThreadNamePrefix("mq-async-send-handler");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
    }

    public <T> void send(String topic, String tag, T t) {
        log.debug("准备发送消息，topic：{}， tag：{}， message：{}", topic, tag, t);
        String destination = topic + ":" + tag;
        rocketMQTemplate.convertAndSend(destination, t);
    }

    public <T> void sendDelayMessage(String topic, String tag, T t, Duration delay) {
        log.debug("准备发送延迟消息，topic：{}， tag：{}， delay：{}， message：{}", topic, tag, delay, t);
        String destination = topic + ":" + tag;
        long delayLevel = convertDurationToDelayLevel(delay);
        Message<T> message = MessageBuilder.withPayload(t)
                .setHeader("delayTimeLevel", delayLevel)
                .build();
        rocketMQTemplate.asyncSend(destination, message, null);
    }

    public <T> void sendAsyn(String topic, String tag, T t, Long time) {
        CompletableFuture.runAsync(() -> {
            try {
                if (time != null && time > 0) {
                    Thread.sleep(time);
                }
                send(topic, tag, t);
            } catch (Exception e) {
                log.error("推送消息异常，t:{},", t, e);
            }
        }, executor);
    }

    public <T> void sendAsyn(String topic, String tag, T t) {
        sendAsyn(topic, tag, t, null);
    }

    private long convertDurationToDelayLevel(Duration delay) {
        long seconds = delay.getSeconds();
        if (seconds <= 1) return 1;
        if (seconds <= 5) return 2;
        if (seconds <= 10) return 3;
        if (seconds <= 30) return 4;
        if (seconds <= 60) return 5;
        if (seconds <= 120) return 6;
        if (seconds <= 300) return 7;
        if (seconds <= 600) return 8;
        if (seconds <= 900) return 9;
        if (seconds <= 1800) return 10;
        if (seconds <= 3600) return 11;
        return 12;
    }
}
