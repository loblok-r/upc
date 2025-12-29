package cn.loblok.upc.worker.mq;

import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.api.worker.dto.NotificationMsg;
import cn.loblok.upc.api.worker.dto.ProductDeliveryMsgDTO;
import cn.loblok.upc.worker.config.RabbitConfig;
import cn.loblok.upc.worker.service.DeliveryService;
import cn.loblok.upc.worker.service.NotifyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class ProductDeliveryConsumer {

    private final NotifyService notifyService;
    private final DeliveryService deliveryService;
    private final UserFeignClient userFeignClient;

    @RabbitListener(queues = RabbitConfig.QUEUE_PRODUCT_DELIVERY)
    public void onMessage(ProductDeliveryMsgDTO msg) {
        try {
            // 发货
            deliveryService.delivery(msg);
            // 发送站内信
            notifyService.createNotification(NotificationMsg.builder()
                    .toUserId(msg.getUserId())
                    .content("您兑换的[" + msg.getProductName() + "]已发放到账")
                    .build());

        } catch (Exception e) {
            // todo 失败则重试（RabbitMQ 默认重试），最终进入死信队列人工处理
            throw e;
        }
    }
}