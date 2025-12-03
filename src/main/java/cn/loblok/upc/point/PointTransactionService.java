package cn.loblok.upc.point;

import cn.loblok.upc.common.dto.PointTransactionDTO;
import cn.loblok.upc.point.PointTransaction;
import cn.loblok.upc.common.enums.BizType;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户积分流水表 服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-01
 */
public interface PointTransactionService extends IService<PointTransaction> {
    void asyncLog(
            String tenantId,
            Long userId,
            BizType bizType,
            Long bizId,          // 注意：传 Long，内部转 String
            int deltaPoints,
            Long balanceAfter
    );

    IPage<PointTransactionDTO> getUserTransactions(
            String tenantId, Long userId, String bizType, int pageNum, int pageSize);

}
