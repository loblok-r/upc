package cn.loblok.upc.auth.service.chickin;

import cn.loblok.upc.auth.dto.chickin.ExpTransactionDTO;
import cn.loblok.upc.auth.entity.chickin.ExpTransaction;
import cn.loblok.upc.common.enums.BizType;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户经验值流水表 服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */
public interface ExpTransactionService extends IService<ExpTransaction> {
    void asyncLog(
            String tenantId,
            Long userId,
            BizType bizType,
            Long bizId,          // 注意：传 Long，内部转 String
            int deltaExps,
            Long balanceAfter
    );

    IPage<ExpTransactionDTO> getUserTransactions(
            String tenantId, Long userId, String bizType, int pageNum, int pageSize);

}
