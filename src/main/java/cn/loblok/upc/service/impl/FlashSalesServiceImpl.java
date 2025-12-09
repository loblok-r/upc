package cn.loblok.upc.service.impl;

import cn.loblok.upc.entity.FlashSales;
import cn.loblok.upc.mapper.FlashSalesMapper;
import cn.loblok.upc.service.FlashSalesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *   秒杀抢购
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Service
public class FlashSalesServiceImpl extends ServiceImpl<FlashSalesMapper, FlashSales> implements FlashSalesService {
    
    @Override
    public boolean updateStock(String flashSaleId, int quantity) {
        // 使用乐观锁更新库存
        FlashSales flashSale = this.getById(flashSaleId);
        if (flashSale == null) {
            return false;
        }
        
        int newStock = flashSale.getRemainingStock() + quantity;
        if (newStock < 0) {
            // 库存不足
            return false;
        }
        
        flashSale.setRemainingStock(newStock);
        // 使用updateById会利用MyBatis Plus的乐观锁机制（需要version字段）
        return this.updateById(flashSale);
    }
}