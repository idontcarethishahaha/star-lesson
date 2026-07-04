package com.tianji.common.autoconfigure.mq;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnClass(value = {RocketMQTemplate.class})
@EnableConfigurationProperties(RocketMQProperties.class)
public class MqConfig {

    @Bean
    @Primary
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rocketmq", value = {"name-server", "producer.group"})
    public RocketMQTemplate rocketMQTemplate(RocketMQProperties rocketMQProperties) {
        RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
        
        DefaultMQProducer producer = new DefaultMQProducer(rocketMQProperties.getProducer().getGroup());
        producer.setNamesrvAddr(rocketMQProperties.getNameServer());
        
        rocketMQTemplate.setProducer(producer);
        return rocketMQTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public RocketMqHelper rocketMqHelper(RocketMQTemplate rocketMQTemplate) {
        return new RocketMqHelper(rocketMQTemplate);
    }
}
