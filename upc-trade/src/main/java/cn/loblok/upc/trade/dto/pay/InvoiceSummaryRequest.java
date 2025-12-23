package cn.loblok.upc.trade.dto.pay;

import lombok.Data;

@Data
public class InvoiceSummaryRequest {

    // 对应前端的 formData
    private UserPayFormData formData;

    // 对应前端的 orderDetails
    private OrderDetailsDTO orderDetails;
}