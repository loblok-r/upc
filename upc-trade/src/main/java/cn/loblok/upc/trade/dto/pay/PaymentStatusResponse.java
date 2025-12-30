package cn.loblok.upc.trade.dto.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentStatusResponse {

    @Schema(description = "订单ID")
    private String orderId;
    @Schema(description = "订单状态")
    private String status;
    @Schema(description = "支付金额")
    private BigDecimal amount;
    @Schema(description = "支付时间")
    private String paidAt;

}