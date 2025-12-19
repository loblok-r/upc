package cn.loblok.upc.modules.payment.strategy;

public interface AliPayService {
    String queryOrderStatus(String orderId);
}