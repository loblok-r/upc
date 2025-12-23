package cn.loblok.upc.trade.task;

import cn.loblok.upc.trade.entity.TOrder;
import cn.loblok.upc.trade.mapper.TOrderMapper;
import cn.loblok.upc.trade.service.TOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：处理订单超时未支付
 */
@Component
@Slf4j
public class OrderTimeoutTask {

    @Autowired
    private TOrderMapper orderMapper;

    // 每分钟检查一次
    @Scheduled(fixedRate = 60_000)
    public void closeExpiredOrders() {
        // 关闭 2 小时前创建、仍未支付的订单
        LocalDateTime expireTime = LocalDateTime.now().minusHours(2);

        List<TOrder> expiredOrders = orderMapper.selectExpiredUnpaidOrders(expireTime);

        for (TOrder order : expiredOrders) {
            int updated = orderMapper.closeOrderIfUnpaid(order.getOrderId());
            if (updated > 0) {
                log.info("订单超时关闭: {}", order.getOrderId());
                // 可选：发消息、释放库存等
            }
        }
    }
}