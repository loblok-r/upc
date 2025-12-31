package cn.loblok.upc.worker.mq;

import cn.loblok.rabbit.util.rabbit.constants.MQConstants;
import cn.loblok.upc.api.worker.dto.NotificationMsg;
import cn.loblok.upc.api.worker.dto.ProductDeliveryMsgDTO;
import cn.loblok.upc.worker.service.DeliveryService;
import cn.loblok.upc.worker.service.NotifyService;
import cn.loblok.rabbit.util.rabbit.util.MessageRetryHelper;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@AllArgsConstructor
public class ProductDeliveryConsumer {

    private final NotifyService notifyService;
    private final DeliveryService deliveryService;
    private final MessageRetryHelper retryHelper;


    @RabbitListener(queues = MQConstants.QUEUE_PRODUCT_DELIVERY
            , ackMode = "MANUAL")
    public void onMessage(ProductDeliveryMsgDTO msg, Message message, Channel channel) {
        retryHelper.processWithRetry(
                message,
                channel,
                () -> {
                    // 发货
                    deliveryService.delivery(msg);
                    // 发送站内信
                    notifyService.createNotification(NotificationMsg.builder()
                            .toUserId(msg.getUserId())
                            .content("您兑换的[" + msg.getProductName() + "]已发放到账")
                            .build());
                },
                MQConstants.RETRY_EXCHANGE_NAME,
                MQConstants.QUEUE_PRODUCT_DELIVERY + ".retry.5s",
                2 // 最多重试 2 次
        );
    }
}