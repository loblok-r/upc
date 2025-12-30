package cn.loblok.upc.worker.config;

import cn.loblok.rabbit.constants.MQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 交换机、队列、绑定关系定义
 */
@Configuration
public class RabbitConfig {

    //生成 DLQ 名称
    private static String dlqName(String queueName) {
        return queueName + ".dlq";
    }

    //生成 5s 重试队列名
    private static String retry5sQueueName(String queueName) {
        return queueName + ".retry.5s";
    }

    // 主交换机
    @Bean
    public DirectExchange upcExchange() {
        return new DirectExchange(MQConstants.EXCHANGE_NAME, true, false);
    }

    //死信交换机
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(MQConstants.DLX_EXCHANGE_NAME, true, false);
    }

    // 重试交换机
    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(MQConstants.RETRY_EXCHANGE_NAME, true, false);
    }

    //邮件
    @Bean public Queue emailQueue() {
        return QueueBuilder.durable(MQConstants.QUEUE_EMAIL)
                .deadLetterExchange(MQConstants.DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(MQConstants.QUEUE_EMAIL))
                .build();
    }
    @Bean public Queue emailRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(MQConstants.QUEUE_EMAIL))
                .withArgument("x-dead-letter-exchange", MQConstants.EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key",MQConstants.ROUTE_EMAIL)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue emailDlq() {
        return QueueBuilder.durable(dlqName(MQConstants.QUEUE_EMAIL)).build();
    }
    @Bean public Binding bindEmail() {
        return BindingBuilder.bind(emailQueue()).to(upcExchange()).with(MQConstants.ROUTE_EMAIL);
    }
    @Bean public Binding bindEmailRetry5s() {
        return BindingBuilder.bind(emailRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(MQConstants.QUEUE_EMAIL));
    }
    @Bean public Binding bindEmailDlq() {
        return BindingBuilder.bind(emailDlq()).to(dlxExchange()).with(dlqName(MQConstants.QUEUE_EMAIL));
    }

    //站内信
    @Bean public Queue siteMsgQueue() {
        return QueueBuilder.durable(MQConstants.QUEUE_SITE_MSG)
                .deadLetterExchange(MQConstants.DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(MQConstants.QUEUE_SITE_MSG))
                .build();
    }
    @Bean public Queue siteMsgRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(MQConstants.QUEUE_SITE_MSG))
                .withArgument("x-dead-letter-exchange", MQConstants.EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", MQConstants.ROUTE_SITE_MSG)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue siteMsgDlq() {
        return QueueBuilder.durable(dlqName(MQConstants.QUEUE_SITE_MSG)).build();
    }
    @Bean public Binding bindSiteMsg() {
        return BindingBuilder.bind(siteMsgQueue()).to(upcExchange()).with(MQConstants.ROUTE_SITE_MSG);
    }
    @Bean public Binding bindSiteMsgRetry5s() {
        return BindingBuilder.bind(siteMsgRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(MQConstants.QUEUE_SITE_MSG));
    }
    @Bean public Binding bindSiteMsgDlq() {
        return BindingBuilder.bind(siteMsgDlq()).to(dlxExchange()).with(dlqName(MQConstants.QUEUE_SITE_MSG));
    }

    //搜索同步
    @Bean public Queue searchSyncQueue() {
        return QueueBuilder.durable(MQConstants.QUEUE_SEARCH_SYNC)
                .deadLetterExchange(MQConstants.DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(MQConstants.QUEUE_SEARCH_SYNC))
                .build();
    }
    @Bean public Queue searchSyncRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(MQConstants.QUEUE_SEARCH_SYNC))
                .withArgument("x-dead-letter-exchange", MQConstants.EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", MQConstants.ROUTE_SEARCH_SYNC)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue searchSyncDlq() {
        return QueueBuilder.durable(dlqName(MQConstants.QUEUE_SEARCH_SYNC)).build();
    }
    @Bean public Binding bindSearchSync() {
        return BindingBuilder.bind(searchSyncQueue()).to(upcExchange()).with(MQConstants.ROUTE_SEARCH_SYNC);
    }
    @Bean public Binding bindSearchSyncRetry5s() {
        return BindingBuilder.bind(searchSyncRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(MQConstants.QUEUE_SEARCH_SYNC));
    }
    @Bean public Binding bindSearchSyncDlq() {
        return BindingBuilder.bind(searchSyncDlq()).to(dlxExchange()).with(dlqName(MQConstants.QUEUE_SEARCH_SYNC));
    }

    // 状态更新
    @Bean public Queue statsUpdateQueue() {
        return QueueBuilder.durable(MQConstants.QUEUE_STATS_UPDATE)
                .deadLetterExchange(MQConstants.DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(MQConstants.QUEUE_STATS_UPDATE))
                .build();
    }
    @Bean public Queue statsUpdateRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(MQConstants.QUEUE_STATS_UPDATE))
                .withArgument("x-dead-letter-exchange", MQConstants.EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", MQConstants.ROUTE_STATS_UPDATE)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue statsUpdateDlq() {
        return QueueBuilder.durable(dlqName(MQConstants.QUEUE_STATS_UPDATE)).build();
    }
    @Bean public Binding bindStatsUpdate() {
        return BindingBuilder.bind(statsUpdateQueue()).to(upcExchange()).with(MQConstants.ROUTE_STATS_UPDATE);
    }
    @Bean public Binding bindStatsUpdateRetry5s() {
        return BindingBuilder.bind(statsUpdateRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(MQConstants.QUEUE_STATS_UPDATE));
    }
    @Bean public Binding bindStatsUpdateDlq() {
        return BindingBuilder.bind(statsUpdateDlq()).to(dlxExchange()).with(dlqName(MQConstants.QUEUE_STATS_UPDATE));
    }

    // AI日志
    @Bean public Queue aiSettleQueue() {
        return QueueBuilder.durable(MQConstants.QUEUE_AI_SETTLE)
                .deadLetterExchange(MQConstants.DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(MQConstants.QUEUE_AI_SETTLE))
                .build();
    }
    @Bean public Queue aiSettleRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(MQConstants.QUEUE_AI_SETTLE))
                .withArgument("x-dead-letter-exchange", MQConstants.EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", MQConstants.ROUTE_AI_SETTLE)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue aiSettleDlq() {
        return QueueBuilder.durable(dlqName(MQConstants.QUEUE_AI_SETTLE)).build();
    }
    @Bean public Binding bindAiSettle() {
        return BindingBuilder.bind(aiSettleQueue()).to(upcExchange()).with(MQConstants.ROUTE_AI_SETTLE);
    }
    @Bean public Binding bindAiSettleRetry5s() {
        return BindingBuilder.bind(aiSettleRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(MQConstants.QUEUE_AI_SETTLE));
    }
    @Bean public Binding bindAiSettleDlq() {
        return BindingBuilder.bind(aiSettleDlq()).to(dlxExchange()).with(dlqName(MQConstants.QUEUE_AI_SETTLE));
    }

    // 产品发货
    @Bean public Queue productDeliveryQueue() {
        return QueueBuilder.durable(MQConstants.QUEUE_PRODUCT_DELIVERY)
                .deadLetterExchange(MQConstants.DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(MQConstants.QUEUE_PRODUCT_DELIVERY))
                .build();
    }
    @Bean public Queue productDeliveryRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(MQConstants.QUEUE_PRODUCT_DELIVERY))
                .withArgument("x-dead-letter-exchange", MQConstants.EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", MQConstants.ROUTE_PRODUCT_DELIVERY)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue productDeliveryDlq() {
        return QueueBuilder.durable(dlqName(MQConstants.QUEUE_PRODUCT_DELIVERY)).build();
    }
    @Bean public Binding bindProductDelivery() {
        return BindingBuilder.bind(productDeliveryQueue()).to(upcExchange()).with(MQConstants.ROUTE_PRODUCT_DELIVERY);
    }
    @Bean public Binding bindProductDeliveryRetry5s() {
        return BindingBuilder.bind(productDeliveryRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(MQConstants.QUEUE_PRODUCT_DELIVERY));
    }
    @Bean public Binding bindProductDeliveryDlq() {
        return BindingBuilder.bind(productDeliveryDlq()).to(dlxExchange()).with(dlqName(MQConstants.QUEUE_PRODUCT_DELIVERY));
    }

    // 积分流水
    @Bean public Queue pointTransactionQueue() {
        return QueueBuilder.durable(MQConstants.QUEUE_POINT_TRANSACTION)
                .deadLetterExchange(MQConstants.DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(MQConstants.QUEUE_POINT_TRANSACTION))
                .build();
    }
    @Bean public Queue pointTransactionRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(MQConstants.QUEUE_POINT_TRANSACTION))
                .withArgument("x-dead-letter-exchange", MQConstants.EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", MQConstants.ROUTE_POINT_TRANSACTION)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue pointTransactionDlq() {
        return QueueBuilder.durable(dlqName(MQConstants.QUEUE_POINT_TRANSACTION)).build();
    }
    @Bean public Binding bindPointTransaction() {
        return BindingBuilder.bind(pointTransactionQueue()).to(upcExchange()).with(MQConstants.ROUTE_POINT_TRANSACTION);
    }
    @Bean public Binding bindPointTransactionRetry5s() {
        return BindingBuilder.bind(pointTransactionRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(MQConstants.QUEUE_POINT_TRANSACTION));
    }
    @Bean public Binding bindPointTransactionDlq() {
        return BindingBuilder.bind(pointTransactionDlq()).to(dlxExchange()).with(dlqName(MQConstants.QUEUE_POINT_TRANSACTION));
    }

    // 经验流水
    @Bean public Queue expTransactionQueue() {
        return QueueBuilder.durable(MQConstants.QUEUE_EXP_TRANSACTION)
                .deadLetterExchange(MQConstants.DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(MQConstants.QUEUE_EXP_TRANSACTION))
                .build();
    }
    @Bean public Queue expTransactionRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(MQConstants.QUEUE_EXP_TRANSACTION))
                .withArgument("x-dead-letter-exchange", MQConstants.EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", MQConstants.ROUTE_EXP_TRANSACTION)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue expTransactionDlq() {
        return QueueBuilder.durable(dlqName(MQConstants.QUEUE_EXP_TRANSACTION)).build();
    }
    @Bean public Binding bindExpTransaction() {
        return BindingBuilder.bind(expTransactionQueue()).to(upcExchange()).with(MQConstants.ROUTE_EXP_TRANSACTION);
    }
    @Bean public Binding bindExpTransactionRetry5s() {
        return BindingBuilder.bind(expTransactionRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(MQConstants.QUEUE_EXP_TRANSACTION));
    }
    @Bean public Binding bindExpTransactionDlq() {
        return BindingBuilder.bind(expTransactionDlq()).to(dlxExchange()).with(dlqName(MQConstants.QUEUE_EXP_TRANSACTION));
    }

    //消息转换器
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}