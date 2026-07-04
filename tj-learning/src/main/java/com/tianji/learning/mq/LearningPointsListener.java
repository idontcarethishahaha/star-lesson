package com.tianji.learning.mq;

import com.tianji.common.constants.MqConstants;
import com.tianji.learning.enums.PointsRecordType;
import com.tianji.learning.mq.message.SignInMessage;
import com.tianji.learning.service.IPointsRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningPointsListener {

    private final IPointsRecordService recordService;

    @RocketMQMessageListener(topic = MqConstants.Exchange.LEARNING_EXCHANGE, consumerGroup = "learning-points-reply-group", selectorExpression = MqConstants.Key.WRITE_REPLY)
    @Component
    public class WriteReplyListener implements RocketMQListener<Long> {
        @Override
        public void onMessage(Long userId) {
            recordService.addPointsRecord(userId, 5, PointsRecordType.QA);
        }
    }

    @RocketMQMessageListener(topic = MqConstants.Exchange.LEARNING_EXCHANGE, consumerGroup = "learning-points-sign-group", selectorExpression = MqConstants.Key.SIGN_IN)
    @Component
    public class SignInListener implements RocketMQListener<SignInMessage> {
        @Override
        public void onMessage(SignInMessage message) {
            recordService.addPointsRecord(message.getUserId(), message.getPoints(), PointsRecordType.SIGN);
        }
    }

    @RocketMQMessageListener(topic = MqConstants.Exchange.LEARNING_EXCHANGE, consumerGroup = "learning-points-learn-group", selectorExpression = MqConstants.Key.LEARN_SECTION)
    @Component
    public class LearnSectionListener implements RocketMQListener<Long> {
        @Override
        public void onMessage(Long userId) {
            recordService.addPointsRecord(userId, 10, PointsRecordType.LEARNING);
        }
    }

    @RocketMQMessageListener(topic = MqConstants.Exchange.LEARNING_EXCHANGE, consumerGroup = "learning-points-note-group", selectorExpression = MqConstants.Key.WRITE_NOTE)
    @Component
    public class WriteNoteListener implements RocketMQListener<Long> {
        @Override
        public void onMessage(Long userId) {
            recordService.addPointsRecord(userId, 3, PointsRecordType.NOTE);
        }
    }

    @RocketMQMessageListener(topic = MqConstants.Exchange.LEARNING_EXCHANGE, consumerGroup = "learning-points-gathered-group", selectorExpression = MqConstants.Key.NOTE_GATHERED)
    @Component
    public class NoteGatheredListener implements RocketMQListener<Long> {
        @Override
        public void onMessage(Long userId) {
            recordService.addPointsRecord(userId, 2, PointsRecordType.NOTE);
        }
    }
}
