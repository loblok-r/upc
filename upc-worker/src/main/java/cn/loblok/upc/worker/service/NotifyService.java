package cn.loblok.upc.worker.service;

import cn.loblok.upc.api.worker.dto.NotificationMsg;
import lombok.extern.slf4j.Slf4j;
import cn.loblok.upc.worker.entity.Notification;
import cn.loblok.upc.worker.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyService {

    private final NotificationMapper notificationMapper;

    /**
     * 创建站内通知记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void createNotification(NotificationMsg msg) {
        log.info("正在持久化站内信: toUserId={}, type={}", msg.getToUserId(), msg.getType());

        try {
            Notification entity = new Notification();
            entity.setFromUserId(msg.getFromUserId());
            entity.setToUserId(msg.getToUserId());
            entity.setType(msg.getType());
            entity.setTargetId(msg.getTargetId());
            entity.setContent(msg.getContent());
            entity.setIsRead(0); // 初始化为未读
            entity.setCreateTime(LocalDateTime.now());

            // 执行插入
            notificationMapper.insert(entity);

            log.debug("站内信持久化成功, ID: {}", entity.getId());
        } catch (Exception e) {
            log.error("保存站内信异常: {}", e.getMessage());
            // 抛出异常让 MQ 重试，保证消息不丢失
            throw new RuntimeException("Notification Persistence Failed", e);
        }
    }
}