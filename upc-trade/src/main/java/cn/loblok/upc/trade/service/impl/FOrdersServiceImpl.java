package cn.loblok.upc.trade.service.impl;

import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.CommonStatusEnum;
import cn.loblok.upc.trade.entity.FlashSaleOrders;
import cn.loblok.upc.common.enums.MallOrderStatus;
import cn.loblok.upc.trade.mapper.FlashOrdersMapper;
import cn.loblok.upc.trade.service.FOrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Service
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
}
