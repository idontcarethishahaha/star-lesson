package com.tianji.search.mq;

import com.tianji.api.dto.trade.OrderBasicDTO;
import com.tianji.common.utils.CollUtils;
import com.tianji.search.service.ICourseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.tianji.common.constants.MqConstants.Exchange.ORDER_EXCHANGE;
import static com.tianji.common.constants.MqConstants.Key.ORDER_PAY_KEY;
import static com.tianji.common.constants.MqConstants.Key.ORDER_REFUND_KEY;

@Slf4j
@Component
public class OrderEventListener {

    @Autowired
    private ICourseService courseService;

    @RocketMQMessageListener(topic = ORDER_EXCHANGE, consumerGroup = "search-order-pay-group", selectorExpression = ORDER_PAY_KEY)
    @Component
    public class OrderPayListener implements RocketMQListener<OrderBasicDTO> {
        @Override
        public void onMessage(OrderBasicDTO order) {
            if (order == null || order.getUserId() == null || CollUtils.isEmpty(order.getCourseIds())) {
                log.debug("订单支付，异常消息，信息未空");
                return;
            }
            log.debug("处理订单支付消息：{}", order);
            courseService.updateCourseSold(order.getCourseIds(), 1);
        }
    }

    @RocketMQMessageListener(topic = ORDER_EXCHANGE, consumerGroup = "search-order-refund-group", selectorExpression = ORDER_REFUND_KEY)
    @Component
    public class OrderRefundListener implements RocketMQListener<OrderBasicDTO> {
        @Override
        public void onMessage(OrderBasicDTO order) {
            if (order == null || order.getUserId() == null || CollUtils.isEmpty(order.getCourseIds())) {
                log.debug("订单退款，异常消息，信息未空");
                return;
            }
            log.debug("处理订单退款消息：{}", order);
            courseService.updateCourseSold(order.getCourseIds(), -1);
        }
    }
}
