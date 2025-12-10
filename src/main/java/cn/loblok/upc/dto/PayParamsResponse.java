package cn.loblok.upc.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

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