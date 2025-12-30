package cn.loblok.upc.worker.mq;

import cn.loblok.rabbit.constants.MQConstants;
import cn.loblok.upc.api.worker.dto.ExpTransactionDTO;
import cn.loblok.upc.common.enums.BizType;
import cn.loblok.upc.worker.config.RabbitConfig;
import cn.loblok.upc.worker.service.ExpTransactionService;
import cn.loblok.upc.worker.util.MessageRetryHelper;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@AllArgsConstructor
public class ExpTransactiionConsumer {

    private final ExpTransactionService expTransactionService;

    private final MessageRetryHelper retryHelper;


    @RabbitListener(queues = MQConstants.QUEUE_EXP_TRANSACTION
                    , ackMode = "MANUAL")
    public void onMessage(ExpTransactionDTO msg, Channel channel, Message message) {
        retryHelper.processWithRetry(
                message,
                channel,
                () -> {
                    expTransactionService.asyncLog(msg.getTenantId(),
                            msg.getUserId(),
                            BizType.valueOf(msg.getBizType()),
                            msg.getBizId(),
                            msg.getDeltaExps(),
                            (long)msg.getTotalExps());
                },
                MQConstants.RETRY_EXCHANGE_NAME,
                MQConstants.QUEUE_EXP_TRANSACTION + ".retry.5s",
                2 // 最多重试 2 次
        );
    }
}