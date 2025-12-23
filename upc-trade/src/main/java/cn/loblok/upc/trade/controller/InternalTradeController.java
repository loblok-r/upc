package cn.loblok.upc.trade.controller;


import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.trade.service.FOrdersService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trade/internal")
@AllArgsConstructor
@Slf4j
public class InternalTradeController {


    private final FOrdersService ordersService;

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 订单状态
     */
    @PostMapping("/orders/updateStatus")
    Result<Void> updateOrderStatus(@RequestParam("orderId")  String orderId, @RequestParam("status") String status){

       return ordersService.updateOrderStatus(orderId, status);
    }
}