package cn.loblok.upc.service.impl;

import cn.loblok.upc.entity.FlashOrders;
import cn.loblok.upc.mapper.FlashOrdersMapper;
import cn.loblok.upc.service.FlashOrdersService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 *    秒杀订单服务
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Service
public class FlashOrdersServiceImpl extends ServiceImpl<FlashOrdersMapper, FlashOrders> implements FlashOrdersService {
    
    @Override
    public boolean hasUserParticipated(Long userId, String flashSaleId) {
        QueryWrapper<FlashOrders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("flash_sale_id", flashSaleId);
        // 如果存在任何状态的订单，则认为用户已参与
        return this.count(queryWrapper) > 0;
    }
    
    @Override
    public boolean cancelExpiredOrder(String orderId) {
        FlashOrders order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        
        // 检查订单是否超时
        if (LocalDateTime.now().isAfter(order.getReserveExpiresAt())) {
            order.setOrderStatus("CANCELLED");
            return this.updateById(order);
        }
        
        return false;
    }
}