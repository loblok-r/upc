package cn.loblok.upc.modules.payment.dto;

import lombok.Data;

@Data
public class PaymentResponse {

    private boolean success;
    private String orderId;
    private String qrCodeUrl; // 扫码支付的二维码内容
    private String paymentUrl; // 跳转支付的链接 (支付宝常用)
    private String expireTime;
}