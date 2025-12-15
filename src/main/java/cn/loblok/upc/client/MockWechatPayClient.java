package cn.loblok.upc.client;

import cn.loblok.upc.dto.WechatNativePayResponse;
import cn.loblok.upc.dto.WechatUnifiedOrderRequest;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary // ← 关键！优先使用这个 Bean
public class MockWechatPayClient  {

    public WechatNativePayResponse unifiedOrderNative(WechatUnifiedOrderRequest request) {
        WechatNativePayResponse response = null;
        response.setCodeUrl("https://mock.qr.code?orderNo=" + request.getOutTradeNo());
        return response;
    }

    public String queryOrderStatus(String orderId) {
        return null;
    }
}