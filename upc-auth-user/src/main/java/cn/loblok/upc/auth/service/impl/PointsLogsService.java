package cn.loblok.upc.auth.service.impl;

import cn.loblok.upc.auth.dto.chickin.PointTransactionDTO;
import cn.loblok.upc.auth.entity.chickin.PointTransaction;
import cn.loblok.upc.auth.mapper.PointTransactionMapper;
import cn.loblok.upc.common.enums.BizType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class PointsLogsService extends ServiceImpl<PointTransactionMapper,  PointTransaction> {

    private static final Set<String> VALID_BIZ_TYPES =
            Arrays.stream(BizType.values())
                    .map(Enum::name)
                    .collect(Collectors.toSet());

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
    public IPage<PointTransactionDTO> getUserTransactions(String tenantId, Long userId, String bizType, int pageNum, int pageSize) {
        Page<PointTransaction> page = new Page<>(pageNum, pageSize);

        QueryWrapper<PointTransaction> query = new QueryWrapper<>();
//        query.eq("tenant_id", tenantId)
//                .eq("user_id", userId);
        query.eq("user_id", userId);
        if (StringUtils.hasText(bizType)) {
            // 安全校验：防止非法枚举值
            if (VALID_BIZ_TYPES.contains(bizType)) {
                query.eq("biz_type", bizType);
            }
        }

        query.orderByDesc("created_at");

        IPage<PointTransaction> result = this.page(page, query);

        log.info("查询用户积分流水，租户ID:{}，用户ID:{}，业务类型:{}，页码:{}，页面大小:{}，结果总数:{}",
                tenantId, userId, bizType, pageNum, pageSize, result.getSize());

        // 转 DTO（可加 bizTypeDesc）
        IPage<PointTransactionDTO> convert = result.convert(tx -> {
            PointTransactionDTO dto = new PointTransactionDTO();
            dto.setId(tx.getId());
            dto.setBizType(tx.getBizType().name());
            dto.setBizTypeDesc(tx.getBizType().getDesc());
            dto.setDeltaPoints(tx.getDeltaPoints());
            dto.setBalanceAfter(tx.getBalanceAfter());
            dto.setCreatedAt(tx.getCreatedAt());
            return dto;
        });
        convert.setTotal(result.getTotal());
        convert.setPages(result.getPages());
        return convert;
    }
}