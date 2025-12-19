package cn.loblok.upc.modules.payment.strategy;

import cn.loblok.upc.modules.payment.client.WechatPayClient;
import cn.loblok.upc.modules.payment.strategy.WeChatPayService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 微信支付服务实现类
 */
@Service
@AllArgsConstructor
public class WeChatPayServiceImpl implements WeChatPayService {
    
    private final WechatPayClient wechatPayClient;

    @Override
    public String queryOrderStatus(String orderId) {
       return wechatPayClient.queryOrderStatus(orderId);
    }
}