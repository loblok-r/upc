package cn.loblok.upc.worker.config;

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

    // 主交换机
    public static final String EXCHANGE_NAME = "upc.direct.exchange";

    // 死信交换机
    public static final String DLX_EXCHANGE_NAME = "upc.dlx.exchange";

    // 重试交换机
    public static final String RETRY_EXCHANGE_NAME = "upc.retry.exchange";

    //队列名称定义
    public static final String QUEUE_EMAIL = "q.notification.email";
    public static final String QUEUE_SITE_MSG = "q.notification.site";
    public static final String QUEUE_SEARCH_SYNC = "q.search.sync";
    public static final String QUEUE_STATS_UPDATE = "q.stats.update";
    public static final String QUEUE_AI_SETTLE = "q.ai.settle";
    public static final String QUEUE_PRODUCT_DELIVERY = "q.product.delivery";
    public static final String QUEUE_POINT_TRANSACTION = "q.point.transaction";
    public static final String QUEUE_EXP_TRANSACTION = "q.exp.transaction";

    //路由键
    public static final String ROUTE_EMAIL = "mq.route.email";
    public static final String ROUTE_SITE_MSG = "mq.route.site_msg";
    public static final String ROUTE_SEARCH_SYNC = "mq.route.search_sync";
    public static final String ROUTE_STATS_UPDATE = "mq.route.stats_update";
    public static final String ROUTE_AI_SETTLE = "mq.route.ai_settle";
    public static final String ROUTE_PRODUCT_DELIVERY = "mq.route.product_delivery";
    public static final String ROUTE_POINT_TRANSACTION = "mq.route.point_transaction";
    public static final String ROUTE_EXP_TRANSACTION = "mq.route.exp_transaction";

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
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    //死信交换机
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE_NAME, true, false);
    }

    // 重试交换机
    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(RETRY_EXCHANGE_NAME, true, false);
    }

    //邮件
    @Bean public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL)
                .deadLetterExchange(DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(QUEUE_EMAIL))
                .build();
    }
    @Bean public Queue emailRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(QUEUE_EMAIL))
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", ROUTE_EMAIL)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue emailDlq() {
        return QueueBuilder.durable(dlqName(QUEUE_EMAIL)).build();
    }
    @Bean public Binding bindEmail() {
        return BindingBuilder.bind(emailQueue()).to(upcExchange()).with(ROUTE_EMAIL);
    }
    @Bean public Binding bindEmailRetry5s() {
        return BindingBuilder.bind(emailRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(QUEUE_EMAIL));
    }
    @Bean public Binding bindEmailDlq() {
        return BindingBuilder.bind(emailDlq()).to(dlxExchange()).with(dlqName(QUEUE_EMAIL));
    }

    //站内信
    @Bean public Queue siteMsgQueue() {
        return QueueBuilder.durable(QUEUE_SITE_MSG)
                .deadLetterExchange(DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(QUEUE_SITE_MSG))
                .build();
    }
    @Bean public Queue siteMsgRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(QUEUE_SITE_MSG))
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", ROUTE_SITE_MSG)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue siteMsgDlq() {
        return QueueBuilder.durable(dlqName(QUEUE_SITE_MSG)).build();
    }
    @Bean public Binding bindSiteMsg() {
        return BindingBuilder.bind(siteMsgQueue()).to(upcExchange()).with(ROUTE_SITE_MSG);
    }
    @Bean public Binding bindSiteMsgRetry5s() {
        return BindingBuilder.bind(siteMsgRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(QUEUE_SITE_MSG));
    }
    @Bean public Binding bindSiteMsgDlq() {
        return BindingBuilder.bind(siteMsgDlq()).to(dlxExchange()).with(dlqName(QUEUE_SITE_MSG));
    }

    //搜索同步
    @Bean public Queue searchSyncQueue() {
        return QueueBuilder.durable(QUEUE_SEARCH_SYNC)
                .deadLetterExchange(DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(QUEUE_SEARCH_SYNC))
                .build();
    }
    @Bean public Queue searchSyncRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(QUEUE_SEARCH_SYNC))
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", ROUTE_SEARCH_SYNC)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue searchSyncDlq() {
        return QueueBuilder.durable(dlqName(QUEUE_SEARCH_SYNC)).build();
    }
    @Bean public Binding bindSearchSync() {
        return BindingBuilder.bind(searchSyncQueue()).to(upcExchange()).with(ROUTE_SEARCH_SYNC);
    }
    @Bean public Binding bindSearchSyncRetry5s() {
        return BindingBuilder.bind(searchSyncRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(QUEUE_SEARCH_SYNC));
    }
    @Bean public Binding bindSearchSyncDlq() {
        return BindingBuilder.bind(searchSyncDlq()).to(dlxExchange()).with(dlqName(QUEUE_SEARCH_SYNC));
    }

    // 状态更新
    @Bean public Queue statsUpdateQueue() {
        return QueueBuilder.durable(QUEUE_STATS_UPDATE)
                .deadLetterExchange(DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(QUEUE_STATS_UPDATE))
                .build();
    }
    @Bean public Queue statsUpdateRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(QUEUE_STATS_UPDATE))
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", ROUTE_STATS_UPDATE)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue statsUpdateDlq() {
        return QueueBuilder.durable(dlqName(QUEUE_STATS_UPDATE)).build();
    }
    @Bean public Binding bindStatsUpdate() {
        return BindingBuilder.bind(statsUpdateQueue()).to(upcExchange()).with(ROUTE_STATS_UPDATE);
    }
    @Bean public Binding bindStatsUpdateRetry5s() {
        return BindingBuilder.bind(statsUpdateRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(QUEUE_STATS_UPDATE));
    }
    @Bean public Binding bindStatsUpdateDlq() {
        return BindingBuilder.bind(statsUpdateDlq()).to(dlxExchange()).with(dlqName(QUEUE_STATS_UPDATE));
    }

    // AI日志
    @Bean public Queue aiSettleQueue() {
        return QueueBuilder.durable(QUEUE_AI_SETTLE)
                .deadLetterExchange(DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(QUEUE_AI_SETTLE))
                .build();
    }
    @Bean public Queue aiSettleRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(QUEUE_AI_SETTLE))
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", ROUTE_AI_SETTLE)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue aiSettleDlq() {
        return QueueBuilder.durable(dlqName(QUEUE_AI_SETTLE)).build();
    }
    @Bean public Binding bindAiSettle() {
        return BindingBuilder.bind(aiSettleQueue()).to(upcExchange()).with(ROUTE_AI_SETTLE);
    }
    @Bean public Binding bindAiSettleRetry5s() {
        return BindingBuilder.bind(aiSettleRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(QUEUE_AI_SETTLE));
    }
    @Bean public Binding bindAiSettleDlq() {
        return BindingBuilder.bind(aiSettleDlq()).to(dlxExchange()).with(dlqName(QUEUE_AI_SETTLE));
    }

    // 产品发货
    @Bean public Queue productDeliveryQueue() {
        return QueueBuilder.durable(QUEUE_PRODUCT_DELIVERY)
                .deadLetterExchange(DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(QUEUE_PRODUCT_DELIVERY))
                .build();
    }
    @Bean public Queue productDeliveryRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(QUEUE_PRODUCT_DELIVERY))
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", ROUTE_PRODUCT_DELIVERY)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue productDeliveryDlq() {
        return QueueBuilder.durable(dlqName(QUEUE_PRODUCT_DELIVERY)).build();
    }
    @Bean public Binding bindProductDelivery() {
        return BindingBuilder.bind(productDeliveryQueue()).to(upcExchange()).with(ROUTE_PRODUCT_DELIVERY);
    }
    @Bean public Binding bindProductDeliveryRetry5s() {
        return BindingBuilder.bind(productDeliveryRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(QUEUE_PRODUCT_DELIVERY));
    }
    @Bean public Binding bindProductDeliveryDlq() {
        return BindingBuilder.bind(productDeliveryDlq()).to(dlxExchange()).with(dlqName(QUEUE_PRODUCT_DELIVERY));
    }

    // 积分流水
    @Bean public Queue pointTransactionQueue() {
        return QueueBuilder.durable(QUEUE_POINT_TRANSACTION)
                .deadLetterExchange(DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(QUEUE_POINT_TRANSACTION))
                .build();
    }
    @Bean public Queue pointTransactionRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(QUEUE_POINT_TRANSACTION))
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", ROUTE_POINT_TRANSACTION)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue pointTransactionDlq() {
        return QueueBuilder.durable(dlqName(QUEUE_POINT_TRANSACTION)).build();
    }
    @Bean public Binding bindPointTransaction() {
        return BindingBuilder.bind(pointTransactionQueue()).to(upcExchange()).with(ROUTE_POINT_TRANSACTION);
    }
    @Bean public Binding bindPointTransactionRetry5s() {
        return BindingBuilder.bind(pointTransactionRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(QUEUE_POINT_TRANSACTION));
    }
    @Bean public Binding bindPointTransactionDlq() {
        return BindingBuilder.bind(pointTransactionDlq()).to(dlxExchange()).with(dlqName(QUEUE_POINT_TRANSACTION));
    }

    // 经验流水
    @Bean public Queue expTransactionQueue() {
        return QueueBuilder.durable(QUEUE_EXP_TRANSACTION)
                .deadLetterExchange(DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(dlqName(QUEUE_EXP_TRANSACTION))
                .build();
    }
    @Bean public Queue expTransactionRetry5sQueue() {
        return QueueBuilder.durable(retry5sQueueName(QUEUE_EXP_TRANSACTION))
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", ROUTE_EXP_TRANSACTION)
                .withArgument("x-message-ttl", 5000)
                .build();
    }
    @Bean public Queue expTransactionDlq() {
        return QueueBuilder.durable(dlqName(QUEUE_EXP_TRANSACTION)).build();
    }
    @Bean public Binding bindExpTransaction() {
        return BindingBuilder.bind(expTransactionQueue()).to(upcExchange()).with(ROUTE_EXP_TRANSACTION);
    }
    @Bean public Binding bindExpTransactionRetry5s() {
        return BindingBuilder.bind(expTransactionRetry5sQueue()).to(retryExchange()).with(retry5sQueueName(QUEUE_EXP_TRANSACTION));
    }
    @Bean public Binding bindExpTransactionDlq() {
        return BindingBuilder.bind(expTransactionDlq()).to(dlxExchange()).with(dlqName(QUEUE_EXP_TRANSACTION));
    }

    //消息转换器
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}