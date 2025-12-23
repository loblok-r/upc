package cn.loblok.upc.auth.service;

import cn.loblok.upc.auth.entity.UserItems;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.UserItemSourceType;
import cn.loblok.upc.common.enums.UserItemType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户虚拟道具/资产表（不含优惠券） 服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-10
 */
public interface UserItemsService extends IService<UserItems> {

    List<UserItems> getByUserId(Long userId, UserItemType userItemType);

    Result<Void> addItem(
            Long userId,
            UserItemType itemType,
            UserItemSourceType sourceType,
            String sourceId,      // 👈 建议加上，用于追踪来源（如订单ID）
            Map<String, Object> extra,  // 👈 所有额外属性放这里
            Integer quantity      // 👈 数量（必须是整数）
    );

    /**
     * 获取道具次数
     *
     * @param userId 用户ID
     * @return 抽奖次数
     */
    int getTotalChances(Long userId, UserItemType itemType);

    /**
     * 扣减道具次数
     *
     * @param id 道具id
     * @return 抽奖次数
     */
    int consumeOneChanceWithOptimisticLock(Long id);


}
