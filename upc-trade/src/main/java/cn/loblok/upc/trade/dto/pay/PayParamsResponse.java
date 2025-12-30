package cn.loblok.upc.trade.dto.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付参数响应体
 */
@Data
@Builder
public class PayParamsResponse {
    @Schema(description = "订单号")
    private String orderNo;
    @Schema(description = "支付金额")
    private BigDecimal amount;
    @Schema(description = "二维码地址")
    private String codeUrl;
}