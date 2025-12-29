package cn.loblok.upc.trade.task;

import cn.loblok.upc.trade.entity.FlashSales;
import cn.loblok.upc.trade.entity.TOrder;
import cn.loblok.upc.trade.mapper.FlashOrdersMapper;
import cn.loblok.upc.trade.mapper.FlashSalesMapper;
import cn.loblok.upc.trade.mapper.TOrderMapper;
import cn.loblok.upc.trade.service.FlashSalesService;
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
    private TOrderMapper torderMapper;

    @Autowired
    private FlashSalesService flashSalesService;

    @Autowired
    private FlashSalesMapper flashSalesMapper;

    @Autowired
    private FlashOrdersMapper forderMapper;



    /**
     * 每分钟执行一次
     * 定时任务：处理订单超时未支付
     */
    @Scheduled(fixedRate = 60_000)
    public void closeExpiredOrders() {
        // 关闭 2 小时前创建、仍未支付的订单
        LocalDateTime expireTime = LocalDateTime.now().minusHours(2);

        List<TOrder> expiredOrders = torderMapper.selectExpiredUnpaidOrders(expireTime);

        for (TOrder order : expiredOrders) {
            int updated = torderMapper.closeOrderIfUnpaid(order.getOrderId());
            if (updated > 0) {
                log.info("订单超时关闭: {}", order.getOrderId());
                // 可选：发消息、释放库存等
            }
        }
    }


    /**
     * 每5分钟执行一次
     * 定时任务：同步已结束的秒杀活动库存到商品表
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncFinishedFlashSales() {
        log.info("开始同步已结束的秒杀活动库存到商品表...");

        List<FlashSales> finishedSales = flashSalesMapper.selectFinishedUnsynced();
        if (finishedSales.isEmpty()) {
            log.info("没有已结束的秒杀活动需要同步库存");
            return;
        }
        for (FlashSales sale : finishedSales) {
            try {
                flashSalesService.syncFlashSaleStockToProduct(sale.getId());
                log.info("活动 {} 库存同步完成，销量: {}", sale.getId(),
                        forderMapper.countSuccessfulOrders(sale.getId()));
            } catch (Exception e) {
                log.error("同步活动库存失败: {}", sale.getId(), e);
                // 可选：记录失败日志 or 发送告警
            }
        }
    }

}