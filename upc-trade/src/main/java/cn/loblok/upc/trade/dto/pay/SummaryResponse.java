package cn.loblok.upc.trade.dto.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SummaryResponse {
    @Schema(description = "支付订单的摘要")
    private String summary;
}