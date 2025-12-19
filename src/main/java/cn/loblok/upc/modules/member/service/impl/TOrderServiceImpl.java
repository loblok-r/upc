package cn.loblok.upc.modules.member.service.impl;

import cn.loblok.upc.modules.member.entity.TOrder;
import cn.loblok.upc.mapper.TOrderMapper;
import cn.loblok.upc.modules.member.service.TOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会员订单表 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-14
 */
@Service
public class TOrderServiceImpl extends ServiceImpl<TOrderMapper, TOrder> implements TOrderService {

}
