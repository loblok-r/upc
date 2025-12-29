package cn.loblok.upc.trade.service;

import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.trade.dto.mall.ExchangeProducesRequest;
import cn.loblok.upc.trade.dto.mall.ProductDTO;
import cn.loblok.upc.trade.entity.Products;
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

    /**
     * 获取商品列表
     *
     * @param page 页码
     * @param size 页面大小
     * @return 商品列表
     */
    Result<PageResult<ProductDTO>> getProductList(int page, int size);

    /**
     * 兑换商品
     *
     * @param userId 用户ID
     * @param request 兑换商品请求
     * @return 兑换结果
     */
    Result<String> exchangeProduct(Long userId, ExchangeProducesRequest request);

    /**
     * 扣减库存
     *
     * @param productId 商品ID
     * @param soldCount 卖出数量
     * @return 是否扣减成功
     */
    boolean deductStock(String productId, int soldCount);

    boolean addStock(String productId, int remainingStock);
}
