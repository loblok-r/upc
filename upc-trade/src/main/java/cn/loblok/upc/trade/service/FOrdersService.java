package cn.loblok.upc.trade.service;

import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.trade.entity.FlashSaleOrders;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
public interface FOrdersService extends IService<FlashSaleOrders> {

    Result<Void> updateOrderStatus(String orderId, String status);
}
