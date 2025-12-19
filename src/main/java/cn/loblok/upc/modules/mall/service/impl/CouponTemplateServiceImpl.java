package cn.loblok.upc.modules.mall.service.impl;

import cn.loblok.upc.modules.mall.service.CouponTemplateService;
import cn.loblok.upc.modules.mall.entity.CouponTemplate;
import cn.loblok.upc.mapper.CouponTemplateMapper;
import cn.loblok.upc.common.util.RedisUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  优惠券模板 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */
@Service
@Primary
@Slf4j
public class CouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper, CouponTemplate> implements CouponTemplateService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    @Transactional
    public void createTemplate(CouponTemplate template) {
        this.save(template); // 先保存到 DB

        // 初始化 Redis 库存（注意：只在新建时设置，避免覆盖）
        String stockKey = RedisUtils.buildCouponStockKey(template.getId());
        Boolean exists = redisTemplate.hasKey(stockKey);
        if (Boolean.FALSE.equals(exists)) {
            redisTemplate.opsForValue().set(stockKey, String.valueOf(template.getTotalStock()));
        }
    }

    @Override
    public CouponTemplate getByCode(String templateCode) {
        return this.lambdaQuery()
                .eq(CouponTemplate::getTemplateCode, templateCode)
                .eq(CouponTemplate::getStatus, 1)
                .one();
    }

@Override
public CouponTemplate getByActivityCode(String activityCode) {
    return this.lambdaQuery()
            .eq(CouponTemplate::getActivityCode, activityCode)
            .eq(CouponTemplate::getStatus, 1)
            .one();
}
}
