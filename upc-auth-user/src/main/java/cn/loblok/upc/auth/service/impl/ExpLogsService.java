package cn.loblok.upc.auth.service.impl;

import cn.loblok.upc.auth.entity.chickin.ExpTransaction;
import cn.loblok.upc.auth.mapper.ExpTransactionMapper;
import cn.loblok.upc.common.enums.BizType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import cn.loblok.upc.auth.dto.chickin.ExpTransactionDTO;


import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ExpLogsService extends ServiceImpl<ExpTransactionMapper, ExpTransaction> {

    private static final Set<String> VALID_BIZ_TYPES =
            Arrays.stream(BizType.values())
                    .map(Enum::name)
                    .collect(Collectors.toSet());
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