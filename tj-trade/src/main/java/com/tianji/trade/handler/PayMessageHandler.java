package com.tianji.trade.handler;

import com.tianji.common.constants.MqConstants;
import com.tianji.pay.sdk.dto.PayResultDTO;
import com.tianji.pay.sdk.dto.RefundResultDTO;
import com.tianji.trade.domain.dto.OrderDelayQueryDTO;
import com.tianji.trade.service.IOrderService;
import com.tianji.trade.service.IPayService;
import com.tianji.trade.service.IRefundApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayMessageHandler {

    private final IOrderService orderService;
    private final IRefundApplyService refundApplyService;
    private final IPayService payService;

    @RocketMQMessageListener(topic = MqConstants.Exchange.PAY_EXCHANGE, consumerGroup = "trade-pay-success-group", selectorExpression = MqConstants.Key.PAY_SUCCESS)
    @Component
    public class PaySuccessListener implements RocketMQListener<PayResultDTO> {
        @Override
        public void onMessage(PayResultDTO payResult) {
            log.debug("收到支付成功通知：{}", payResult);
            orderService.handlePaySuccess(payResult);
        }
    }

    @RocketMQMessageListener(topic = MqConstants.Exchange.PAY_EXCHANGE, consumerGroup = "trade-refund-result-group", selectorExpression = MqConstants.Key.REFUND_CHANGE)
    @Component
    public class RefundResultListener implements RocketMQListener<RefundResultDTO> {
        @Override
        public void onMessage(RefundResultDTO refundResult) {
            log.debug("收到退款变更成功通知：{}", refundResult);
            refundApplyService.handleRefundResult(refundResult);
        }
    }

    @RocketMQMessageListener(topic = MqConstants.Exchange.TRADE_DELAY_EXCHANGE, consumerGroup = "trade-delay-order-group", selectorExpression = MqConstants.Key.ORDER_DELAY_KEY)
    @Component
    public class OrderDelayQueryListener implements RocketMQListener<OrderDelayQueryDTO> {
        @Override
        public void onMessage(OrderDelayQueryDTO message) {
            log.debug("收到订单延迟查询通知：{}", message);
            payService.queryPayResult(message);
        }
    }
}
