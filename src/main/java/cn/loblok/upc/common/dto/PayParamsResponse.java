package cn.loblok.upc.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class PayParamsResponse {
    private String orderNo;
    private BigDecimal amount;
    private String codeUrl;
}