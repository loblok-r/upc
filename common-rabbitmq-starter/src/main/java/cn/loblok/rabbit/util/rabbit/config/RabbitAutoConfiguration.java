package cn.loblok.rabbit.util.rabbit.config;

import cn.loblok.rabbit.util.rabbit.event.MessageConfirmEvent;
import cn.loblok.rabbit.util.rabbit.util.MessageRetryHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass(RabbitTemplate.class)
class RabbitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RabbitTemplate.class)
    public RabbitTemplate rabbitTemplate(
            CachingConnectionFactory connectionFactory,
            ApplicationEventPublisher eventPublisher,
            MessageConverter messageConverter) {

        // 启用 Publisher Confirm（Spring Boot 3.x 方式）
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        // 启用 Return Callback
        connectionFactory.setPublisherReturns(true);

        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        template.setMessageConverter(messageConverter);
        // Confirm 回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            String bizId = (correlationData != null && correlationData.getId() != null)
                    ? correlationData.getId()
                    : "unknown";
            if (ack) {
                log.debug("消息确认成功, bizId: {}", bizId);
            } else {
                log.warn("消息确认失败, bizId: {}, cause: {}", bizId, cause);
            }
            eventPublisher.publishEvent(new MessageConfirmEvent(this, bizId, ack, cause));
        });

        // Return 回调
        template.setReturnsCallback(returned -> {
            log.warn("消息无法路由: exchange={}, routingKey={}, replyText={}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyText());
        });
        // 必须设置 mandatory=true，ReturnCallback 才会触发
        template.setMandatory(true);

        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageRetryHelper messageRetryHelper(RabbitTemplate rabbitTemplate) {
        return new MessageRetryHelper(rabbitTemplate);
    }

    @Bean
    @ConditionalOnMissingBean // 如果业务微服务自己定义了转换器，则以业务为准，否则用这个默认的
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}