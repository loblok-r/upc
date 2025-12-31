package cn.loblok.upc.trade.consumer;

import cn.hutool.core.util.IdUtil;
import cn.loblok.rabbit.util.rabbit.constants.MQConstants;
import cn.loblok.rabbit.util.rabbit.util.MessageRetryHelper;
import cn.loblok.upc.api.worker.dto.ProductDeliveryMsgDTO;
import cn.loblok.upc.api.worker.dto.SeckillOrderCreateDTO;
import cn.loblok.upc.common.enums.UserItemSourceType;
import cn.loblok.upc.trade.entity.FlashSaleOrders;
import cn.loblok.upc.trade.entity.Products;
import cn.loblok.upc.trade.service.FOrdersService;
import cn.loblok.upc.trade.service.ProductsService;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@Slf4j
@AllArgsConstructor
public class OrderCreateConsumer {

    private final FOrdersService fordersService;
    private final MessageRetryHelper retryHelper;

    private final ProductsService productsService;

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstants.QUEUE_ORDER_CREATE, durable = "true"),
            exchange = @Exchange(value = MQConstants.EXCHANGE_NAME),
            key = MQConstants.ROUTE_ORDER_CREATE
    ), ackMode = "MANUAL", concurrency = "1-3")
    public void onMessage(SeckillOrderCreateDTO msg, Message message, Channel channel) {
        log.info("创建秒杀订单：{}", msg);
        retryHelper.processWithRetry(
                message,
                channel,
                () -> {
                    // 幂等检查
                    if (fordersService.isOrderExisted(msg.getUserId(), msg.getFlashSaleId())) {
                        log.warn("订单已存在，跳过重复创建: {}", msg.getTraceId());
                        return;
                    }
                    // 创建真实订单并存入数据库
                    FlashSaleOrders order = fordersService.saveSeckillOrder(msg);

                    // 注册事务同步：只有当本地订单事务 commit 成功后，才发送发货 MQ
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            sendDeliveryMessage(order, msg.getUserId());
                        }
                    });

                },
                MQConstants.RETRY_EXCHANGE_NAME,
                MQConstants.QUEUE_ORDER_CREATE + ".retry.5",
                2 // 最多重试 2 次
        );
    }
    /**
     * 发送 MQ 秒杀成功消息
     *
     * @param order
     * @param userId
     */
    private void sendDeliveryMessage(FlashSaleOrders order, Long userId) {

        String productId = order.getProductId();
        Products product = productsService.getById(productId);
        String bizId = IdUtil.randomUUID();
        CorrelationData correlationData = new CorrelationData(bizId);
        // 发送 MQ 消息，任务结束，立即返回给前端
        ProductDeliveryMsgDTO msg = ProductDeliveryMsgDTO.builder()
                .orderId(order.getId())
                .userId(userId)
                .category(product.getCategory().name())
                .productName(product.getName())
                .source(UserItemSourceType.FLASH_SALE.getDescription())
                .deliveryConfig(product.getDeliveryConfig())
                .build();
        rabbitTemplate.convertAndSend(
                MQConstants.EXCHANGE_NAME,
                MQConstants.ROUTE_PRODUCT_DELIVERY,
                msg,
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                },
                correlationData
        );
    }
}