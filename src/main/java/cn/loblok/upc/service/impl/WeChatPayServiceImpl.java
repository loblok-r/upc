package cn.loblok.upc.service.impl;

import cn.loblok.upc.client.WechatPayClient;
import cn.loblok.upc.service.WeChatPayService;
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