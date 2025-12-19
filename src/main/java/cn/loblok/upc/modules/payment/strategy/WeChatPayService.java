package cn.loblok.upc.modules.payment.strategy;

public interface WeChatPayService {
    String queryOrderStatus(String orderId);
}
