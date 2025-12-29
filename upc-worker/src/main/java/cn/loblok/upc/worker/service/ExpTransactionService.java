package cn.loblok.upc.worker.service;

import cn.loblok.upc.common.enums.BizType;
import cn.loblok.upc.worker.entity.ExpTransaction;
import cn.loblok.upc.worker.mapper.ExpTransactionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;


/**
 * <p>
 * 用户经验值流水表 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */
@Service
@Primary
@Slf4j
public class ExpTransactionService {


    @Autowired
    private ExpTransactionMapper expTransactionMapper; // MyBatis Mapper

    private static final ZoneId BUSINESS_TIMEZONE = ZoneId.of("Asia/Shanghai");


    /**
     * 异步记录经验流水（生产级安全）
     */
    public void asyncLog(
            String tenantId,
            Long userId,
            BizType bizType,
            Long bizId,
            int deltaExps,
            Long balanceAfter
    ) {

        LocalDateTime curDateTime = LocalDateTime.now(BUSINESS_TIMEZONE);
        // 1. 构造实体
        ExpTransaction tx = new ExpTransaction();
        tx.setUserId(userId);
        tx.setTenantId(tenantId);
        tx.setBizType(bizType.name()); // 修正：使用枚举名称而非枚举本身
        tx.setBizId(String.valueOf(bizId)); // 转为字符串存储
        tx.setDeltaExp(deltaExps);
        tx.setBalanceAfter(balanceAfter);
        tx.setCreatedAt(curDateTime);
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                expTransactionMapper.insert(tx);
                return; // 成功就退出
            } catch (Exception e) {
                if (i == maxRetries - 1) {
                    log.error("【严重】经验流水写入失败，已重试{}次", maxRetries, e);
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