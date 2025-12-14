package cn.loblok.upc.client;

import cn.loblok.upc.enums.TOrderStatus;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import cn.loblok.upc.dto.WechatNativePayResponse;
import cn.loblok.upc.dto.WechatUnifiedOrderRequest;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 微信支付客户端（使用官方SDK）
 */
@Service
public class WechatPayClient {

    @Autowired
    private NativePayService nativePayService;

    @Value("${wechat.pay.app-id}")
    private String appId;

    @Value("${wechat.pay.mch-id}")
    private String merchantId;

    @Value("${wechat.pay.notify-url}")
    private String notifyUrl;

    /**
     * 微信 Native 支付下单
     */
    public WechatNativePayResponse unifiedOrderNative(WechatUnifiedOrderRequest request) {
        PrepayRequest prepayRequest = new PrepayRequest();
        prepayRequest.setAppid(appId);
        prepayRequest.setMchid(merchantId);
        prepayRequest.setDescription(request.getDescription());
        prepayRequest.setOutTradeNo(request.getOutTradeNo());
        prepayRequest.setNotifyUrl(notifyUrl);

        Amount amount = new Amount();
        amount.setTotal(request.getTotalFee());
        prepayRequest.setAmount(amount);

        // 调用 NativePayService 的 prepay 方法发起支付
        PrepayResponse response = nativePayService.prepay(prepayRequest);
        
        return new WechatNativePayResponse(response.getCodeUrl());
    }


    /**
     * 查询微信订单状态
     */
    public String queryOrderStatus(String orderId) {
        try {
            QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            request.setOutTradeNo(orderId);

            Transaction transaction = nativePayService.queryOrderByOutTradeNo(request);

            switch (transaction.getTradeState()) {
                case SUCCESS:
                    return TOrderStatus.SUCCESS.getCode();
                case NOTPAY:
                case USERPAYING:
                    return TOrderStatus.PENDING.getCode();
                case REVOKED:
                case CLOSED:
                case PAYERROR:
                    return TOrderStatus.FAILED.getCode();
                default:
                    return TOrderStatus.PENDING.getCode();
            }
        } catch (Exception e) {
            throw new RuntimeException("查询微信订单状态失败", e);
        }
    }
}