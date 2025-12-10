package cn.loblok.upc.service;

import cn.loblok.upc.entity.FlashOrders;
import cn.loblok.upc.entity.Products;

/**
 * 方法服务接口
 */
public interface DeliveryService {

    // 1. 实物商品：不立即发货，只更新订单状态
    void deliverPhysical(FlashOrders order);

    // 2. 虚拟权益：直接写用户账户（本站会员、算力值等）
    void deliverVirtual(FlashOrders order, Products product);

    // 3. 凭证/道具：发到用户资产表（补签卡、抽奖次数、优惠券等）
    void deliverVoucher(FlashOrders order, Products product);

    // 特殊处理：第三方会员（可选单独方法或在 virtual 中判断）
    void deliverThirdPartyVirtual(FlashOrders order, Products product) ;

}
