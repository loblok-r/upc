package cn.loblok.upc.worker.mq;

import cn.loblok.upc.api.worker.dto.ExpTransactionDTO;
import cn.loblok.upc.common.enums.BizType;
import cn.loblok.upc.worker.config.RabbitConfig;
import cn.loblok.upc.worker.service.ExpTransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class ExpTransactiionConsumer {

    private final ExpTransactionService expTransactionService;

    @RabbitListener(queues = RabbitConfig.QUEUE_EXP_TRANSACTION)
    public void onMessage(ExpTransactionDTO msg) {

        expTransactionService.asyncLog(msg.getTenantId(),
                msg.getUserId(),
                BizType.valueOf(msg.getBizType()),
                msg.getBizId(),
                msg.getDeltaExps(),
                (long)msg.getTotalExps()
                );

    }

}