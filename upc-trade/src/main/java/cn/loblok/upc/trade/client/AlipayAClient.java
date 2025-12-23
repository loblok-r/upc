package cn.loblok.upc.trade.client;

import cn.loblok.upc.common.enums.MembershipOrderStatus;
import cn.loblok.upc.trade.dto.pay.PaymentResponse;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 支付宝支付客户端（使用官方SDK）
 */
@Service
public class AlipayAClient {

    @Autowired
    private AlipayClient alipayClient;

    /**
     * 创建支付宝支付订单
     * @param orderId 订单ID
     * @param amount 支付金额
     * @param description 订单描述
     * @return 支付响应
     */
    public PaymentResponse createAlipayOrder(String orderId, BigDecimal amount, String description) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + orderId + "\"," +
                "\"total_amount\":" + amount + "," +
                "\"subject\":\"" + description + "\"," +
                "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"" +
                "}");
        
        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setOrderId(orderId);
            paymentResponse.setSuccess(true);
            paymentResponse.setPaymentUrl(response.getBody());
            return paymentResponse;
        } catch (AlipayApiException e) {
            throw new RuntimeException("创建支付宝订单失败", e);
        }
    }
    
    /**
     * 查询支付宝订单状态
     * @param orderId 订单ID
     * @return 订单状态
     */
    public String queryOrderStatus(String orderId) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + orderId + "\"" +
                "}");
        
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                switch (response.getTradeStatus()) {
                    case "TRADE_SUCCESS":
                    case "TRADE_FINISHED":
                        return MembershipOrderStatus.PAID.getValue();
                    case "WAIT_BUYER_PAY":
                        return MembershipOrderStatus.PAYING.getValue();
                    case "TRADE_CLOSED":
                        return MembershipOrderStatus.CLOSED.getValue();
                    default:
                        return MembershipOrderStatus.PAYING.getValue();
                }
            } else {
                return MembershipOrderStatus.FAILED.getValue();
            }
        } catch (AlipayApiException e) {
            throw new RuntimeException("查询支付宝订单状态失败", e);
        }
    }
}