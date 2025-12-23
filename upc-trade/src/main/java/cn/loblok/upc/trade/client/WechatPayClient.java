package cn.loblok.upc.trade.client;

import cn.loblok.upc.common.enums.MembershipOrderStatus;
import cn.loblok.upc.trade.dto.pay.WechatNativePayResponse;
import cn.loblok.upc.trade.dto.pay.WechatUnifiedOrderRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 微信支付客户端（使用官方SDK）
 */
@Service
public class WechatPayClient {

    @Autowired(required = false)
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
                    return MembershipOrderStatus.PAID.getValue();
                case NOTPAY:
                case USERPAYING:
                    return MembershipOrderStatus.PAYING.getValue();

                case CLOSED:
                    return MembershipOrderStatus.CLOSED.getValue();
                case REVOKED:
                case PAYERROR:
                    return MembershipOrderStatus.FAILED.getValue();
                default:
                    return MembershipOrderStatus.PAYING.getValue();
            }
        } catch (Exception e) {
            throw new RuntimeException("查询微信订单状态失败", e);
        }
    }
}