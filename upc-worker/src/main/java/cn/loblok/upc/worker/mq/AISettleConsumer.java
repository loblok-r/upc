package cn.loblok.upc.worker.mq;

import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.api.worker.dto.AiSettleDTO;
import cn.loblok.upc.worker.config.RabbitConfig;
import cn.loblok.upc.worker.service.AiLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class AISettleConsumer {

    private final UserFeignClient userFeignClient;

    private final AiLogService aiLogService;

    @RabbitListener(queues = RabbitConfig.QUEUE_AI_SETTLE)
    public void onAISettleMessage(AiSettleDTO msg) {

        // 扣减算力
        userFeignClient.consumeComputerPower(msg.getUserId(), msg.getCost());

        log.info("用户 {} 扣减了 {} 算力", msg.getUserId(), msg.getCost());

        // 本地记录日志
        aiLogService.saveLog(msg.getUserId(),
                msg.getMode(), msg.getPrompt(),
                msg.getCost(), msg.getRefImage(),
                msg.getContent(), msg.getCosPath(),msg.getSessionId());

    }
}