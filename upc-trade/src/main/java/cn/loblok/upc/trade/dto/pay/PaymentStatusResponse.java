package cn.loblok.upc.trade.dto.pay;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentStatusResponse {

    private String orderId;
    private String status;
    private BigDecimal amount;
    private String paidAt;

}