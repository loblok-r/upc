package cn.loblok.upc.client;

import cn.loblok.upc.dto.PaymentResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 支付宝支付客户端（模拟实现）
 */
@Service
public class AlipayClient {

    /**
     * 模拟创建支付宝支付订单
     * @param orderId 订单ID
     * @param amount 支付金额
     * @param description 订单描述
     * @return 支付响应
     */
    public PaymentResponse createAlipayOrder(String orderId, BigDecimal amount, String description) {
        PaymentResponse response = new PaymentResponse();
        response.setOrderId(orderId);
        response.setSuccess(true);
        // 模拟支付宝支付链接
        response.setPaymentUrl("https://open.alipay.com/gateway.do?orderId=" + orderId + "&amount=" + amount);
        return response;
    }
}