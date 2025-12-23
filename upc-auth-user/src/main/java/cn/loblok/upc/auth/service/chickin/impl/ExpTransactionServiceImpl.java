package cn.loblok.upc.auth.service.chickin.impl;

import cn.loblok.upc.auth.dto.chickin.ExpTransactionDTO;
import cn.loblok.upc.auth.entity.chickin.ExpTransaction;
import cn.loblok.upc.auth.service.chickin.ExpTransactionService;
import cn.loblok.upc.common.enums.BizType;
import cn.loblok.upc.auth.mapper.ExpTransactionMapper;
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
 * 用户经验值流水表 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */
@Service
@Primary
@Slf4j
public class ExpTransactionServiceImpl extends ServiceImpl<ExpTransactionMapper, ExpTransaction> implements ExpTransactionService {


    @Autowired
    private ExpTransactionMapper expTransactionMapper; // MyBatis Mapper

    private static final ZoneId BUSINESS_TIMEZONE = ZoneId.of("Asia/Shanghai");
    private static final Set<String> VALID_BIZ_TYPES =
            Arrays.stream(BizType.values())
                    .map(Enum::name)
                    .collect(Collectors.toSet());

    /**
     * 异步记录经验流水（生产级安全）
     */
    @Async("expTaskExecutor") // ← 指定线程池
    @Override
    public void asyncLog(
            String tenantId,
            Long userId,
            BizType bizType,
            Long bizId,          // 注意：传 Long，内部转 String
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


    /**
     * 分页查询用户经验流水
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @param bizType 业务类型（可选）
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 经验流水分页数据
     */
    @Override
    public IPage<ExpTransactionDTO> getUserTransactions(String tenantId, Long userId, String bizType, int pageNum, int pageSize) {
        Page<ExpTransaction> page = new Page<>(pageNum, pageSize);

        QueryWrapper<ExpTransaction> query = new QueryWrapper<>();
        query.eq("tenant_id", tenantId)
                .eq("user_id", userId);

        if (StringUtils.hasText(bizType)) {
            // 安全校验：防止非法枚举值
            if (VALID_BIZ_TYPES.contains(bizType)) {
                query.eq("biz_type", bizType);
            }
        }

        query.orderByDesc("created_at");

        IPage<ExpTransaction> result = this.page(page, query);

        // 转 DTO（可加 bizTypeDesc）
        return result.convert(tx -> {
            ExpTransactionDTO dto = new ExpTransactionDTO();
            dto.setId(tx.getId());
            dto.setBizType(tx.getBizType());
            // 修正：使用BizType枚举获取描述
            if (tx.getBizType() != null) {
                try {
                    BizType type = BizType.valueOf(tx.getBizType());
                    dto.setBizTypeDesc(type.getDesc());
                } catch (IllegalArgumentException e) {
                    dto.setBizTypeDesc("未知类型");
                }
            }
            dto.setDeltaExps(tx.getDeltaExp());
            dto.setBalanceAfter(tx.getBalanceAfter());
            dto.setCreatedAt(tx.getCreatedAt());
            return dto;
        });
    }
}