package cn.loblok.upc.worker.mq;

import cn.loblok.rabbit.constants.MQConstants;
import cn.loblok.upc.worker.util.MessageRetryHelper;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import cn.loblok.upc.api.worker.dto.UserSyncDTO;
import cn.loblok.upc.worker.config.RabbitConfig;
import cn.loblok.upc.worker.service.SearchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;


@Component
@Slf4j
@AllArgsConstructor
public class SearchSyncConsumer {

    private  final SearchService searchService;


    private final MessageRetryHelper retryHelper;

    /**
     * 监听用户搜索同步队列
     */
    @RabbitListener(queues = MQConstants.QUEUE_SEARCH_SYNC
            , ackMode = "MANUAL")
    public void onUserSyncMessage(UserSyncDTO msg, Message message, Channel channel) {
        retryHelper.processWithRetry(
                message,
                channel,
                () -> {
                    if ("DELETE".equalsIgnoreCase(msg.getOperation())) {
                        searchService.deleteUser(msg.getId());
                    } else {
                        // UPSERT 模式：新增或修改
                        // 注意：如果 status 为禁用状态，也可以选择直接从 Meilisearch 删除
                        if (msg.getStatus() != null && msg.getStatus() == 1) {
                            searchService.deleteUser(msg.getId());
                        } else {
                            searchService.upsertUser(msg);
                        }
                    }
                },
                MQConstants.RETRY_EXCHANGE_NAME,
                MQConstants.QUEUE_SEARCH_SYNC + ".retry.5s",
                2 // 最多重试 2 次
        );
    }
}