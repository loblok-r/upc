package cn.loblok.upc.trade.dto.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class InvoiceSummaryRequest {

    @Schema(description = "发票信息")
    // 对应前端的 formData
    private UserPayFormData formData;

    @Schema(description = "订单信息")
    // 对应前端的 orderDetails
    private OrderDetailsDTO orderDetails;
}