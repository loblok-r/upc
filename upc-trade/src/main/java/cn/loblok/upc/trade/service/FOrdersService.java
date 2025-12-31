package cn.loblok.upc.trade.service;

import cn.loblok.upc.api.worker.dto.SeckillOrderCreateDTO;
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

    /**
     * 统计成功订单数
     * @param flashSaleId
     * @return
     */
    int countSuccessfulOrders(String flashSaleId);

    /**
     * 订单是否存在
     * @param userId
     * @param flashSaleId
     * @return
     */
    boolean isOrderExisted(Long userId, String flashSaleId);

    /**
     * 保存秒杀订单
     * @param msg
     * @return
     */
    FlashSaleOrders saveSeckillOrder(SeckillOrderCreateDTO msg);
}
