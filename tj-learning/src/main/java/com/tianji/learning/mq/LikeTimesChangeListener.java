package com.tianji.learning.mq;

import com.tianji.api.dto.remark.LikedTimesDTO;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.service.IInteractionReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tianji.common.constants.MqConstants.Exchange.LIKE_RECORD_EXCHANGE;
import static com.tianji.common.constants.MqConstants.Key.QA_LIKED_TIMES_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeTimesChangeListener {

    private final IInteractionReplyService replyService;

    @RocketMQMessageListener(topic = LIKE_RECORD_EXCHANGE, consumerGroup = "learning-liked-times-group", selectorExpression = QA_LIKED_TIMES_KEY)
    @Component
    public class ReplyLikedTimesListener implements RocketMQListener<List<LikedTimesDTO>> {
        @Override
        public void onMessage(List<LikedTimesDTO> likedTimesDTOs) {
            log.debug("监听到回答或评论的点赞数变更");
            List<InteractionReply> list = new ArrayList<>(likedTimesDTOs.size());
            for (LikedTimesDTO dto : likedTimesDTOs) {
                InteractionReply r = new InteractionReply();
                r.setId(dto.getBizId());
                r.setLikedTimes(dto.getLikedTimes());
                list.add(r);
            }
            replyService.updateBatchById(list);
        }
    }
}
