package cn.loblok.upc.trade.strategy;

import cn.loblok.upc.trade.client.WechatPayClient;
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