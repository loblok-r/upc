package cn.loblok.upc.trade.service.impl;

import cn.loblok.upc.api.worker.dto.SeckillOrderCreateDTO;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.CommonStatusEnum;
import cn.loblok.upc.common.exception.BizException;
import cn.loblok.upc.trade.entity.FlashSaleOrders;
import cn.loblok.upc.common.enums.MallOrderStatus;
import cn.loblok.upc.trade.mapper.FlashOrdersMapper;
import cn.loblok.upc.trade.service.FOrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Service
@Slf4j
public class FOrdersServiceImpl extends ServiceImpl<FlashOrdersMapper, FlashSaleOrders> implements FOrdersService {


    public FOrdersServiceImpl() {
    }
    /**
     * 更新订单状态
     *
     * @param orderId
     * @param status
     * @return
     */
    @Override
    public Result<Void> updateOrderStatus(String orderId, String status) {
        FlashSaleOrders order = this.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        order.setMallOrderStatus(MallOrderStatus.valueOf(status));
        boolean updated = this.updateById(order);

        if (updated) {
            return Result.success("更新成功");
        } else {
            return Result.error(CommonStatusEnum.ORDER_UPDATE_ERROR.getCode(),
                    CommonStatusEnum.ORDER_UPDATE_ERROR.getMessage());
        }
    }

    @Override
    public int countSuccessfulOrders(String flashSaleId) {
        return baseMapper.countSuccessfulOrders(flashSaleId);
    }

    @Override
    public boolean isOrderExisted(Long userId, String flashSaleId) {
        return baseMapper.existsByUserIdAndFlashSaleId(userId, flashSaleId);
    }

    /**
     * 创建秒杀订单
     *
     * @param msg
     * @return
     */
    @Override
    public FlashSaleOrders saveSeckillOrder(SeckillOrderCreateDTO msg) {

        log.info("订单创建开始，userId={}, flashSaleId={}", msg.getUserId(), msg.getFlashSaleId());

        String flashSaleId = msg.getFlashSaleId();
        Long userId = msg.getUserId();
        // 创建订单
        FlashSaleOrders order = new FlashSaleOrders();
        order.setUserId(userId);
        order.setPointsSpent(msg.getPayPoints());
        order.setFlashSaleId(flashSaleId);
        order.setProductId(msg.getProductId());
        //等待发放
        order.setMallOrderStatus(MallOrderStatus.PENDING_EXTERNAL);
        //没有过期时间
        order.setReserveExpiresAt(null);
        order.setCreatedAt(LocalDateTime.now());

        if (!this.save(order)) {
            log.error("订单创建失败，userId={}, flashSaleId={}", userId, flashSaleId);
            // 1. 记录到“待补偿表” 或
            // 2. 直接抛异常，由上层告警 + 对账任务处理
            throw new BizException("系统繁忙，请稍后查看订单");
        }
        return order;
    }
}
