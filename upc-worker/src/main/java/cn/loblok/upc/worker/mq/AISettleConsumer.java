package cn.loblok.upc.worker.mq;

import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.api.worker.dto.AiSettleDTO;
import cn.loblok.upc.worker.config.RabbitConfig;
import cn.loblok.upc.worker.service.AiLogService;
import cn.loblok.upc.worker.util.MessageRetryHelper;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@AllArgsConstructor
public class AISettleConsumer {

    private final UserFeignClient userFeignClient;

    private final AiLogService aiLogService;

    private final MessageRetryHelper retryHelper;
    @RabbitListener(queues = RabbitConfig.QUEUE_AI_SETTLE,
                    ackMode = "MANUAL")
    public void onAISettleMessage(AiSettleDTO msg, Message message, Channel channel) {
        retryHelper.processWithRetry(
                message,
                channel,
                () -> {
                    userFeignClient.consumeComputerPower(msg.getUserId(), msg.getCost());
                    log.info("用户 {} 扣减了 {} 算力", msg.getUserId(), msg.getCost());

                    aiLogService.saveLog(msg.getUserId(), msg.getMode(), msg.getPrompt(),
                            msg.getCost(), msg.getRefImage(), msg.getContent(),
                            msg.getCosPath(), msg.getSessionId());
                },
                RabbitConfig.RETRY_EXCHANGE_NAME,
                RabbitConfig.QUEUE_AI_SETTLE + ".retry.5s",
                2 // 最多重试 2 次
        );
    }
}