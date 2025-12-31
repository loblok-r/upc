package cn.loblok.upc.worker.mq;

import cn.loblok.rabbit.util.rabbit.constants.MQConstants;
import cn.loblok.upc.api.worker.dto.PointTransactionDTO;
import cn.loblok.upc.common.enums.BizType;
import cn.loblok.upc.worker.service.PointTransactionService;
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
public class PointTransactionConsumer {


    private final PointTransactionService pointTransactionService;
    private final MessageRetryHelper retryHelper;


    @RabbitListener(queues = MQConstants.QUEUE_POINT_TRANSACTION
            , ackMode = "MANUAL")
    public void onMessage(PointTransactionDTO msg, Message message, Channel channel) {
        log.info("积分流水消息：{}", msg);
        retryHelper.processWithRetry(
                message,
                channel,
                () -> {
                        pointTransactionService.asyncLog(
                        msg.getTenantId(),
                        msg.getUserId(),
                        BizType.valueOf(msg.getBizType()),
                        msg.getBizId(),
                        msg.getDeltaPoints(),
                        (long) msg.getTotalPoints());
                },
                MQConstants.RETRY_EXCHANGE_NAME,
                MQConstants.QUEUE_POINT_TRANSACTION + ".retry.5",
                2 // 最多重试 2 次
        );
    }
}