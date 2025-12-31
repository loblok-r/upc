package cn.loblok.rabbit.util.rabbit.constants;

// 在公共模块中定义
public interface MQConstants {
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
    public static final String QUEUE_ORDER_CREATE = "q.order.create";

    //路由键
    public static final String ROUTE_EMAIL = "mq.route.email";
    public static final String ROUTE_SITE_MSG = "mq.route.site_msg";
    public static final String ROUTE_SEARCH_SYNC = "mq.route.search_sync";
    public static final String ROUTE_STATS_UPDATE = "mq.route.stats_update";
    public static final String ROUTE_AI_SETTLE = "mq.route.ai_settle";
    public static final String ROUTE_PRODUCT_DELIVERY = "mq.route.product_delivery";
    public static final String ROUTE_POINT_TRANSACTION = "mq.route.point_transaction";
    public static final String ROUTE_EXP_TRANSACTION = "mq.route.exp_transaction";
    public static final String ROUTE_ORDER_CREATE = "mq.route.order_create";
}