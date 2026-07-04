package com.tianji.message.handler;

import com.tianji.api.dto.sms.SmsInfoDTO;
import com.tianji.common.constants.MqConstants;
import com.tianji.message.service.ISmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsMessageHandler {

    private final ISmsService smsService;

    @RocketMQMessageListener(topic = MqConstants.Exchange.SMS_EXCHANGE, consumerGroup = "message-sms-group", selectorExpression = MqConstants.Key.SMS_MESSAGE)
    @Component
    public class SmsListener implements RocketMQListener<SmsInfoDTO> {
        @Override
        public void onMessage(SmsInfoDTO smsInfoDTO) {
            smsService.sendMessage(smsInfoDTO);
        }
    }
}
