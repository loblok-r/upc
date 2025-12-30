package cn.loblok.upc.trade.dto.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    @Schema(description = "订单ID")
    private String orderId;
    @Schema(description = "支付方式")
    private String paymentMethod;
    @Schema(description = "订单描述")
    private String description;
    @Schema(description = "订阅计划ID")
    private String planId;

    // 在 DTO 里直接定义为 BigDecimal
    @Schema(description = "金额")
    private BigDecimal amount;
}