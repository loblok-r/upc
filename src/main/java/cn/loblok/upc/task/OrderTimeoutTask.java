package cn.loblok.upc.task;

import cn.loblok.upc.membershiporder.MembershipOrder;
import cn.loblok.upc.membershiporder.MembershipOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTimeoutTask {

    @Autowired
    private MembershipOrderMapper orderMapper;

    // 每分钟检查一次
    @Scheduled(fixedRate = 60_000)
    public void closeExpiredOrders() {
        // 关闭 2 小时前创建、仍未支付的订单
        LocalDateTime expireTime = LocalDateTime.now().minusHours(2);

        List<MembershipOrder> expiredOrders = orderMapper.selectExpiredUnpaidOrders(expireTime);

        for (MembershipOrder order : expiredOrders) {
            int updated = orderMapper.closeOrderIfUnpaid(order.getId());
            if (updated > 0) {
                log.info("订单超时关闭: {}", order.getOrderNo());
                // 可选：发消息、释放库存等
            }
        }
    }
}