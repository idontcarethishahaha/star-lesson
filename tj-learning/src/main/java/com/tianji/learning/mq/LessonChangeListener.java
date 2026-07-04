package com.tianji.learning.mq;

import com.tianji.api.dto.trade.OrderBasicDTO;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.utils.CollUtils;
import com.tianji.learning.service.ILearningLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LessonChangeListener {

    private final ILearningLessonService lessonService;

    @RocketMQMessageListener(topic = MqConstants.Exchange.ORDER_EXCHANGE, consumerGroup = "learning-lesson-pay-group", selectorExpression = MqConstants.Key.ORDER_PAY_KEY)
    @Component
    public class LessonPayListener implements RocketMQListener<OrderBasicDTO> {
        @Override
        public void onMessage(OrderBasicDTO order) {
            if (order == null || order.getUserId() == null || CollUtils.isEmpty(order.getCourseIds())) {
                log.error("接收到MQ消息有误，订单数据为空");
                return;
            }
            log.debug("监听到用户{}的订单{}，需要添加课程{}到课表中", order.getUserId(), order.getOrderId(), order.getCourseIds());
            lessonService.addUserLessons(order.getUserId(), order.getCourseIds());
        }
    }

    @RocketMQMessageListener(topic = MqConstants.Exchange.ORDER_EXCHANGE, consumerGroup = "learning-lesson-refund-group", selectorExpression = MqConstants.Key.ORDER_REFUND_KEY)
    @Component
    public class LessonRefundListener implements RocketMQListener<OrderBasicDTO> {
        @Override
        public void onMessage(OrderBasicDTO order) {
            if (order == null || order.getUserId() == null || CollUtils.isEmpty(order.getCourseIds())) {
                log.error("接收到MQ消息有误，订单数据为空");
                return;
            }
            log.debug("监听到用户{}的订单{}要退款，需要删除课程{}", order.getUserId(), order.getOrderId(), order.getCourseIds());
            lessonService.deleteCourseFromLesson(order.getUserId(), order.getCourseIds().get(0));
        }
    }
}
