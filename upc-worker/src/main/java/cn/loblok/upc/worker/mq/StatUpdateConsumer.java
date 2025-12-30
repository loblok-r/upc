package cn.loblok.upc.worker.mq;


import cn.loblok.upc.api.worker.dto.StatUpdateMsgDTO;
import cn.loblok.upc.worker.config.RabbitConfig;
import cn.loblok.upc.worker.service.StatService;
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
public class StatUpdateConsumer {


    private final StatService statService;


    private final MessageRetryHelper retryHelper;


    @RabbitListener(queues = RabbitConfig.QUEUE_STATS_UPDATE
            ,ackMode = "MANUAL")
    public void onStatUpdateMessage(StatUpdateMsgDTO msg, Message message, Channel channel) {
        log.info("【统计更新】收到任务: userId={}, type={}, delta={}",
                msg.getUserId(), msg.getType(), msg.getDelta());

        retryHelper.processWithRetry(
                message,
                channel,
                () -> {
                    switch (msg.getType()) {
                        case "LIKE":     // 获赞
                            statService.updateUserLikes(msg.getUserId(), msg.getDelta());
                            break;
                        case "POST":     // 作品数
                            statService.updateUserWorks(msg.getUserId(), msg.getDelta());
                            break;
                        case "FOLLOW":   // 粉丝数
                            statService.updateUserFollowings(msg.getUserId(), msg.getDelta());
                            if (msg.getTargetUserId() != null) {
                                statService.updateUserFollowers(msg.getTargetUserId(), msg.getDelta());
                            }
                            break;
                        default:
                            log.warn("未知统计类型: {}", msg.getType());
                    }
                },
                RabbitConfig.RETRY_EXCHANGE_NAME,
                RabbitConfig.QUEUE_STATS_UPDATE + ".retry.5s",
                2 // 最多重试 2 次
        );
    }
}