package cn.loblok.upc.service.impl;

import cn.loblok.upc.client.AlipayAClient;
import cn.loblok.upc.service.AliPayService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 支付宝服务实现类
 */
@Service
@AllArgsConstructor
public class AliPayServiceImpl implements AliPayService {
    

    private final AlipayAClient alipayAClient;

    @Override
    public String queryOrderStatus(String orderId) {
        return alipayAClient.queryOrderStatus(orderId);
    }
}