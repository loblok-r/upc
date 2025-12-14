package cn.loblok.upc.dto;

import lombok.Data;

@Data
public class PaymentStatusResponse {

    private String orderId;
    private String status; // PENDING, SUCCESS, FAILED, EXPIRED
    private String paidAt;

}