package com.tianji.message.api.client;

import com.tianji.common.autoconfigure.mq.RocketMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.message.domain.dto.SmsInfoDTO;

public class AsyncSmsClient {
    private final RocketMqHelper mqHelper;

    public AsyncSmsClient(RocketMqHelper mqHelper) {
        this.mqHelper = mqHelper;
    }

    /**
     * 基于 MQ 异步发送短信
     * @param smsInfoDTO 短信相关信息
     */
    public void sendMessage(SmsInfoDTO smsInfoDTO){
        mqHelper.send(MqConstants.Exchange.SMS_EXCHANGE, MqConstants.Key.SMS_MESSAGE, smsInfoDTO);
    }
}
