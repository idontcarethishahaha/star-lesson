package com.tianji.search.mq;

import com.tianji.search.service.ICourseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.tianji.common.constants.MqConstants.Exchange.COURSE_EXCHANGE;
import static com.tianji.common.constants.MqConstants.Key.*;

@Slf4j
@Component
public class CourseEventListener {

    @Autowired
    private ICourseService courseService;

    @RocketMQMessageListener(topic = COURSE_EXCHANGE, consumerGroup = "search-course-up-group", selectorExpression = COURSE_UP_KEY)
    @Component
    public class CourseUpListener implements RocketMQListener<Long> {
        @Override
        public void onMessage(Long courseId) {
            log.debug("监听到课程{}上架", courseId);
            courseService.handleCourseUp(courseId);
        }
    }

    @RocketMQMessageListener(topic = COURSE_EXCHANGE, consumerGroup = "search-course-down-group", selectorExpression = COURSE_DOWN_KEY)
    @Component
    public class CourseDownListener implements RocketMQListener<Long> {
        @Override
        public void onMessage(Long courseId) {
            log.debug("监听到课程{}下架", courseId);
            courseService.handleCourseDelete(courseId);
        }
    }

    @RocketMQMessageListener(topic = COURSE_EXCHANGE, consumerGroup = "search-course-expire-group", selectorExpression = COURSE_EXPIRE_KEY)
    @Component
    public class CourseExpireListener implements RocketMQListener<Long> {
        @Override
        public void onMessage(Long courseId) {
            courseService.handleCourseDelete(courseId);
        }
    }
}
