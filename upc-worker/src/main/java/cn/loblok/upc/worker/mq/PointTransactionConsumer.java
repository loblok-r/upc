package cn.loblok.upc.worker.mq;

import cn.loblok.upc.api.worker.dto.PointTransactionDTO;
import cn.loblok.upc.common.enums.BizType;
import cn.loblok.upc.worker.config.RabbitConfig;
import cn.loblok.upc.worker.service.PointTransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class PointTransactionConsumer {


    private final PointTransactionService pointTransactionService;

    @RabbitListener(queues = RabbitConfig.QUEUE_POINT_TRANSACTION)
    public void onMessage(PointTransactionDTO msg) {
        log.info("积分流水消息：{}", msg);
        pointTransactionService.asyncLog(msg.getTenantId(),
                msg.getUserId(),
                BizType.valueOf(msg.getBizType()),
                msg.getBizId(),
                msg.getDeltaPoints(),
                (long)msg.getTotalPoints());
    }
}