package cn.loblok.upc.service.impl;


import cn.loblok.upc.dto.PointTransactionDTO;
import cn.loblok.upc.enums.BizType;
import cn.loblok.upc.entity.PointTransaction;
import cn.loblok.upc.mapper.PointTransactionMapper;
import cn.loblok.upc.service.PointTransactionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
public class PointTransactionServiceImpl extends ServiceImpl<PointTransactionMapper, PointTransaction> implements PointTransactionService {

    @Autowired
    private PointTransactionMapper pointTransactionMapper; // MyBatis Mapper

    private static final ZoneId BUSINESS_TIMEZONE = ZoneId.of("Asia/Shanghai");
    private static final Set<String> VALID_BIZ_TYPES =
            Arrays.stream(BizType.values())
                    .map(Enum::name)
                    .collect(Collectors.toSet());

    /**
     * 异步记录积分流水（生产级安全）
     */
    @Async("pointTaskExecutor") // ← 指定线程池
    @Override
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

    /**
     * 分页查询用户积分流水
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @param bizType 业务类型（可选）
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 积分流水分页数据
     */
    @Override
    public IPage<PointTransactionDTO> getUserTransactions(String tenantId, Long userId, String bizType, int pageNum, int pageSize) {
        Page<PointTransaction> page = new Page<>(pageNum, pageSize);

        QueryWrapper<PointTransaction> query = new QueryWrapper<>();
        query.eq("tenant_id", tenantId)
                .eq("user_id", userId);

        if (StringUtils.hasText(bizType)) {
            // 安全校验：防止非法枚举值
            if (VALID_BIZ_TYPES.contains(bizType)) {
                query.eq("biz_type", bizType);
            }
        }

        query.orderByDesc("created_at");

        IPage<PointTransaction> result = this.page(page, query);

        // 转 DTO（可加 bizTypeDesc）
        return result.convert(tx -> {
            PointTransactionDTO dto = new PointTransactionDTO();
            dto.setId(tx.getId());
            dto.setBizType(tx.getBizType().name());
            dto.setBizTypeDesc(tx.getBizType().getDesc());
            dto.setDeltaPoints(tx.getDeltaPoints());
            dto.setBalanceAfter(tx.getBalanceAfter());
            dto.setCreatedAt(tx.getCreatedAt());
            return dto;
        });
    }
}
