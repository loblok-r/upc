package cn.loblok.upc.modules.mall.service.impl;

import cn.loblok.upc.modules.mall.entity.FlashSaleOrders;
import cn.loblok.upc.modules.mall.entity.Products;
import cn.loblok.upc.mapper.ProductsMapper;
import cn.loblok.upc.modules.mall.service.DeliveryService;
import cn.loblok.upc.modules.mall.service.ProductsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
 * <p>
 *     商品服务
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Service
@Slf4j
@AllArgsConstructor
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements ProductsService {

    private final DeliveryService deliveryService;

    @Override
    // ProductsService.java
    public Products drawRandomPrize() {
        QueryWrapper<Products> query = new QueryWrapper<>();
        query.eq("status", "active")
                .eq("lottery_eligible", true);

        List<Products> prizes = this.list(query);

        if (prizes.isEmpty()) {
            throw new RuntimeException("当前无可抽奖奖品");
        }

        // todo 简单随机，实际可加权重
        Random random = new Random();
        return prizes.get(random.nextInt(prizes.size()));
    }


    /**
     * 商品订单成功后的处理逻辑
     *
     * @param order 订单信息
     * @param product 商品信息
     */

    @Transactional
    public void onOrderSuccess(FlashSaleOrders order, Products product) {


        log.info("用户{}下订单成功，商品{},进入发放逻辑", order.getUserId(), product.getName());

        // 特殊处理：第三方会员（即使类型是 VIRTUAL）
        if (isThirdPartyProduct(product)) {
            deliveryService.deliverThirdPartyVirtual(order, product);
            return;
        }

        // 普通分发
        switch (product.getCategory()) {
            case PHYSICAL:
                deliveryService.deliverPhysical(order);
                break;
            case VIRTUAL:
                deliveryService.deliverVirtual(order, product);
                break;
            case VOUCHER:
                deliveryService.deliverVoucher(order, product);
                break;
            default:
                throw new IllegalStateException("未知商品类型: " + product.getCategory());
        }
    }


    //todo 暂未实现
    private boolean isThirdPartyProduct(Products product) {

        return false;
    }
}
