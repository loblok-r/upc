package cn.loblok.upc.auth.event.handler;

import cn.hutool.core.util.IdUtil;
import cn.loblok.rabbit.constants.MQConstants;
import cn.loblok.upc.api.worker.dto.UserSyncDTO;
import cn.loblok.upc.auth.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SearchSyncHandler {

    private final RabbitTemplate rabbitTemplate;

    @Async // 异步处理，不阻塞注册主流程
    @EventListener
    public void handleSearchSync(UserRegisteredEvent event) {
        log.info("监听到注册事件，准备同步用户信息到 Meilisearch: userId={}", event.getUserId());

        // 构造发送给 MQ 的 DTO
        UserSyncDTO userSyncDTO = new UserSyncDTO();
        userSyncDTO.setId(event.getUserId());
        userSyncDTO.setUsername(event.getUsername());
        userSyncDTO.setAvatarUrl(event.getAvatar()); // 建议 Event 里也带上头像
        userSyncDTO.setStatus(0); // 初始正常状态
        userSyncDTO.setOperation("INSERT");

        String bizId = IdUtil.randomUUID();
        CorrelationData correlationData = new CorrelationData(bizId);
        // 发送到之前定义的交换机
        rabbitTemplate.convertAndSend(
                MQConstants.EXCHANGE_NAME,
                MQConstants.ROUTE_SEARCH_SYNC,
                userSyncDTO,
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                },
                correlationData
        );
    }
}