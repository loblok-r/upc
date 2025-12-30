package cn.loblok.upc.trade.controller;


import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.trade.service.FOrdersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "内部贸易接口", description = "内部贸易接口")
public class InternalTradeController {


    private final FOrdersService ordersService;

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 订单状态
     */
    @PostMapping("/orders/updateStatus")
    @Operation(summary = "更新订单状态")
    Result<Void> updateOrderStatus(@RequestParam("orderId")  String orderId, @RequestParam("status") String status){

       return ordersService.updateOrderStatus(orderId, status);
    }
}