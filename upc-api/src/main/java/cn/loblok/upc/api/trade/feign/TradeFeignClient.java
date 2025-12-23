package cn.loblok.upc.api.trade.feign;


import cn.loblok.upc.api.constant.ServiceNameConstants;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.MallOrderStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = ServiceNameConstants.TRADE_SERVICE, path = "/api/trade/internal")
public interface TradeFeignClient {


    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 订单状态
     */
    @PostMapping("/orders/updateStatus")
    Result<Void> updateOrderStatus(@RequestParam("orderId")  String orderId, @RequestParam("status") String status);
}