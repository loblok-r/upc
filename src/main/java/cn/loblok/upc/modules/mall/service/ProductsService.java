package cn.loblok.upc.modules.mall.service;

import cn.loblok.upc.modules.mall.entity.FlashSaleOrders;
import cn.loblok.upc.modules.mall.entity.Products;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
public interface ProductsService extends IService<Products> {

    Products drawRandomPrize();


    void onOrderSuccess(FlashSaleOrders order, Products product);
}
