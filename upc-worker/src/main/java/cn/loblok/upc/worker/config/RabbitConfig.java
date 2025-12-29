package cn.loblok.upc.worker.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 交换机、队列、绑定关系定义
 */
@Configuration
public class RabbitConfig {

    // 交换机
    public static final String EXCHANGE_NAME = "upc.direct.exchange";


    // 队列
    public static final String QUEUE_EMAIL = "q.notification.email";
    public static final String QUEUE_SITE_MSG = "q.notification.site";
    public static final String QUEUE_SEARCH_SYNC = "q.search.sync";
    public static final String QUEUE_STATS_UPDATE = "q.stats.update";
    public static final String QUEUE_AI_SETTLE = "q.ai.settle";
    public static final String QUEUE_PRODUCT_DELIVERY = "q.product.delivery";
    public  static final String QUEUE_POINT_TRANSACTION = "q.point.transaction";
    public  static final String QUEUE_EXP_TRANSACTION = "q.exp.transaction";


    // 路由键
    public static final String ROUTE_EMAIL = "mq.route.email";
    public static final String ROUTE_SITE_MSG = "mq.route.site_msg";
    public static final String ROUTE_SEARCH_SYNC = "mq.route.search_sync";
    public static final String ROUTE_STATS_UPDATE = "mq.route.stats_update";
    public static final String ROUTE_AI_SETTLE = "mq.route.ai_settle";
    public static final String ROUTE_PRODUCT_DELIVERY = "mq.route.product_delivery";
    public static final String ROUTE_POINT_TRANSACTION = "mq.route.point_transaction";
    public static final String ROUTE_EXP_TRANSACTION = "mq.route.exp_transaction";



    @Bean
    public DirectExchange upcExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }


    // --- 定义队列 ---

    /**
     * 邮件队列，发送注册/重置验证码邮件
     * @return
     */
    @Bean public Queue emailQueue() { return new Queue(QUEUE_EMAIL, true); }
    /**
     * 站内信队列，站内信入库（点赞、评论提醒）
     * @return
     */
    @Bean public Queue siteMsgQueue() { return new Queue(QUEUE_SITE_MSG, true); }

    /**
     * 搜索同步队列，同步用户/帖子数据到 Meilisearch
     * @return
     */
    @Bean public Queue searchSyncQueue() { return new Queue(QUEUE_SEARCH_SYNC, true); }

    /**
     * 统计更新队列，异步更新冗余计数（获赞数、作品数）
     * @return
     */
    @Bean public Queue statsUpdateQueue() { return new Queue(QUEUE_STATS_UPDATE, true); }


    /**
     * AI 结算队列，异步结算用户 AI 消耗
     * @return
     */

    @Bean public Queue aiSettleQueue() { return new Queue(QUEUE_AI_SETTLE, true); }
    /**
     * 商品发放队列，异步发放商品
     * @return
     */
    @Bean public Queue productDeliveryQueue() { return new Queue(QUEUE_PRODUCT_DELIVERY, true); }

    @Bean public Queue pointTransactionQueue() { return new Queue(QUEUE_POINT_TRANSACTION, true); }


    @Bean public Queue expTransactionQueue() { return new Queue(QUEUE_EXP_TRANSACTION, true); }

    // --- 绑定关系 ---
    @Bean
    public Binding bindEmail() {
        return BindingBuilder.bind(emailQueue()).to(upcExchange()).with(ROUTE_EMAIL);
    }

    @Bean
    public Binding bindSiteMsg() {
        return BindingBuilder.bind(siteMsgQueue()).to(upcExchange()).with(ROUTE_SITE_MSG);
    }

    @Bean
    public Binding bindSearchSync() {
        return BindingBuilder.bind(searchSyncQueue()).to(upcExchange()).with(ROUTE_SEARCH_SYNC);
    }

    @Bean
    public Binding bindStatsUpdate() {
        return BindingBuilder.bind(statsUpdateQueue()).to(upcExchange()).with(ROUTE_STATS_UPDATE);
    }

    @Bean
    public Binding bindAiSettle() {
        return BindingBuilder.bind(aiSettleQueue()).to(upcExchange()).with(ROUTE_AI_SETTLE);
    }

    @Bean
    public Binding bindProductDelivery() {
        return BindingBuilder.bind(productDeliveryQueue()).to(upcExchange()).with(ROUTE_PRODUCT_DELIVERY);
    }

    @Bean
    public Binding bindPointTransaction() {
        return BindingBuilder.bind(pointTransactionQueue()).to(upcExchange()).with(ROUTE_POINT_TRANSACTION);
    }

    @Bean
    public Binding bindExpTransaction() {
        return BindingBuilder.bind(expTransactionQueue()).to(upcExchange()).with(ROUTE_EXP_TRANSACTION);
    }


    // 4G 内存关键配置：消息序列化器（使用 JSON 替代 JDK 序列化，省内存且跨语言）
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}