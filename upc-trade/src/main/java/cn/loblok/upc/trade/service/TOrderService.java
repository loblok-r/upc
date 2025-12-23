package cn.loblok.upc.trade.service;


import cn.loblok.upc.trade.entity.TOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-14
 */
public interface TOrderService extends IService<TOrder> {

    void handleWechatPayNotify(String xmlData);

    String handleAliPayNotify(Map<String, String> params);

    void deliveryMember(TOrder tOrder);
}
