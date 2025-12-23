package cn.loblok.upc.trade.strategy;

import cn.loblok.upc.trade.client.AlipayAClient;
import lombok.AllArgsConstructor;
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