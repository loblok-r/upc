package cn.loblok.upc.trade.dto.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PaymentResponse {

    @Schema(description = "支付结果")
    private boolean success;
    @Schema(description = "订单ID")
    private String orderId;
    @Schema(description = "支付结果描述")
    private String qrCodeUrl; // 扫码支付的二维码内容
    @Schema(description = "支付链接")
    private String paymentUrl; // 跳转支付的链接 (支付宝常用)
    @Schema(description = "过期时间")
    private String expireTime;
}