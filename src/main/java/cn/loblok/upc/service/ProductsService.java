package cn.loblok.upc.service;

import cn.loblok.upc.entity.Orders;
import cn.loblok.upc.entity.Products;
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


    void onOrderSuccess(Orders order, Products product);
}
