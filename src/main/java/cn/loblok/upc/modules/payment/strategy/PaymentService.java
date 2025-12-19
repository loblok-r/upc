package cn.loblok.upc.modules.payment.strategy;

import cn.loblok.upc.modules.payment.dto.PaymentResponse;
import cn.loblok.upc.modules.payment.dto.PaymentStatusResponse;

import java.math.BigDecimal;

public interface PaymentService {

    /**
     * 创建支付订单
     * @param orderId 订单ID
     * @param price 订单价格
     * @param paymentMethod 支付方式
     * @param description 订单描述
     * @return 创建结果
     */
    PaymentResponse createOrder(String orderId, BigDecimal price, String paymentMethod, String description);

    /**
     * 查询订单状态
     * @param orderId 订单ID
     * @return 是否成功
     */
    PaymentStatusResponse getPaymentStatus(String orderId);

    /**
     * 取消支付订单
     * @param orderId 订单ID
     * @return 是否成功
     */
    void cancelPayment(String orderId);
}
