package cn.loblok.upc.service.impl;


import cn.loblok.upc.dto.PaymentResponse;
import cn.loblok.upc.dto.PaymentStatusResponse;
import cn.loblok.upc.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService {


    @Override
    public PaymentResponse createOrder(String orderId, BigDecimal price, String paymentMethod, String description) {
        return null;
    }

    @Override
    public PaymentStatusResponse getPaymentStatus(String orderId) {
        return null;
    }

    @Override
    public void cancelPayment(String orderId) {



    }
}