package cn.loblok.upc.trade.dto.pay;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付参数响应体
 */
@Data
@Builder
public class PayParamsResponse {
    private String orderNo;
    private BigDecimal amount;
    private String codeUrl;
}