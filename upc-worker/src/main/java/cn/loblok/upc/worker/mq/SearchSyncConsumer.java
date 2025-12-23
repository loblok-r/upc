package cn.loblok.upc.worker.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import cn.loblok.upc.api.worker.dto.UserSyncDTO;
import cn.loblok.upc.worker.config.RabbitConfig;
import cn.loblok.upc.worker.service.SearchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

@Component
@Slf4j
public class SearchSyncConsumer {

    @Autowired
    private SearchService searchService;

    /**
     * 监听用户搜索同步队列
     */
    @RabbitListener(queues = RabbitConfig.QUEUE_SEARCH_SYNC)
    public void onUserSyncMessage(UserSyncDTO msg) {
        log.info("【搜索同步】收到消息: id={}, op={}", msg.getId(), msg.getOperation());

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
    }
}