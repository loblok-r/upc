package cn.loblok.upc.service.impl;

import cn.loblok.upc.entity.Products;
import cn.loblok.upc.mapper.ProductsMapper;
import cn.loblok.upc.service.ProductsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * <p>
 *     商品服务
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Service
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements ProductsService {

    @Override
    // ProductsService.java
    public Products drawRandomPrize() {
        QueryWrapper<Products> query = new QueryWrapper<>();
        query.eq("status", "active")
                .eq("lottery_eligible", true);

        List<Products> prizes = this.list(query);

        if (prizes.isEmpty()) {
            throw new RuntimeException("当前无可抽奖奖品");
        }

        // todo 简单随机，实际可加权重
        Random random = new Random();
        return prizes.get(random.nextInt(prizes.size()));
    }
}
