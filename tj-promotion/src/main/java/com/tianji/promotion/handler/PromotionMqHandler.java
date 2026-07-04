package com.tianji.promotion.handler;

import com.tianji.promotion.domain.dto.UserCouponDTO;
import com.tianji.promotion.service.IUserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import static com.tianji.common.constants.MqConstants.Exchange.PROMOTION_EXCHANGE;
import static com.tianji.common.constants.MqConstants.Key.COUPON_RECEIVE;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionMqHandler {

    private final IUserCouponService userCouponService;

    @RocketMQMessageListener(topic = PROMOTION_EXCHANGE, consumerGroup = "promotion-coupon-receive-group", selectorExpression = COUPON_RECEIVE)
    @Component
    public class CouponReceiveListener implements RocketMQListener<UserCouponDTO> {
        @Override
        public void onMessage(UserCouponDTO uc) {
            userCouponService.checkAndCreateUserCoupon(uc);
        }
    }
}
