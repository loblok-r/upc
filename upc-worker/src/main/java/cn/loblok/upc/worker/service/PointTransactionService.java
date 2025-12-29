package cn.loblok.upc.worker.service;


import cn.loblok.upc.common.enums.BizType;
import cn.loblok.upc.worker.entity.PointTransaction;
import cn.loblok.upc.worker.mapper.PointTransactionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户积分流水表 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-01
 */
@Service
@Primary
@Slf4j
public class PointTransactionService {

    @Autowired
    private PointTransactionMapper pointTransactionMapper; // MyBatis Mapper

    private static final ZoneId BUSINESS_TIMEZONE = ZoneId.of("Asia/Shanghai");


    /**
     * 异步记录积分流水（生产级安全）
     */
    public void asyncLog(
            String tenantId,
            Long userId,
            BizType bizType,
            Long bizId,          // 注意：传 Long，内部转 String
            int deltaPoints,
            Long balanceAfter
    ) {

        LocalDateTime curDateTime = LocalDateTime.now(BUSINESS_TIMEZONE);
        // 1. 构造实体
        PointTransaction tx = new PointTransaction();
        tx.setUserId(userId);
        tx.setTenantId(tenantId);
        tx.setBizType(bizType);
        tx.setBizId(String.valueOf(bizId)); // 转为字符串存储
        tx.setDeltaPoints(deltaPoints);
        tx.setBalanceAfter(balanceAfter);
        tx.setCreatedAt(curDateTime);
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                pointTransactionMapper.insert(tx);
                return; // 成功就退出
            } catch (Exception e) {
                if (i == maxRetries - 1) {
                    log.error("【严重】积分流水写入失败，已重试{}次", maxRetries, e);
                    // TODO: 发送企业微信/钉钉告警
                } else {
                    try {
                        Thread.sleep(100 * (i + 1)); // 100ms, 200ms, 300ms
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }
}
