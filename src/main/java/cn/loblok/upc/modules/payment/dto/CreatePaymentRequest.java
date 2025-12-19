package cn.loblok.upc.modules.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    private String orderId;
    private String paymentMethod;
    private String description;
    private String planId;

    // 在 DTO 里直接定义为 BigDecimal
    private BigDecimal amount;
}