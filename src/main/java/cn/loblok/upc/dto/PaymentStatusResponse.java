package cn.loblok.upc.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentStatusResponse {

    private String orderId;
    private String status; // PENDING, SUCCESS, FAILED, EXPIRED
    private BigDecimal amount;
    private String paidAt;

}