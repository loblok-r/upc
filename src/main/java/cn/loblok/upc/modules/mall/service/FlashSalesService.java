package cn.loblok.upc.modules.mall.service;

import cn.loblok.upc.modules.mall.entity.FlashSales;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
public interface FlashSalesService extends IService<FlashSales> {
    
    /**
     * 更新秒杀活动库存
     * 
     * @param flashSaleId 秒杀活动ID
     * @param quantity 更新数量（减少库存时为负数）
     * @return 是否更新成功
     */
    boolean updateStock(String flashSaleId, int quantity);
}