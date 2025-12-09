package cn.loblok.upc.service;

import cn.loblok.upc.entity.FlashOrders;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
public interface FlashOrdersService extends IService<FlashOrders> {
    
    /**
     * 检查用户是否已经参与过指定秒杀活动
     * 
     * @param userId 用户ID
     * @param flashSaleId 秒杀活动ID
     * @return 是否已经参与过
     */
    boolean hasUserParticipated(Long userId, String flashSaleId);
    
    /**
     * 取消超时未支付的订单
     * 
     * @param orderId 订单ID
     * @return 是否取消成功
     */
    boolean cancelExpiredOrder(String orderId);
}