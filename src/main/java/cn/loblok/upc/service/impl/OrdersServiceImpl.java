package cn.loblok.upc.service.impl;

import cn.loblok.upc.entity.Orders;
import cn.loblok.upc.mapper.FlashOrdersMapper;
import cn.loblok.upc.service.OrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<FlashOrdersMapper, Orders> implements OrdersService {

}
