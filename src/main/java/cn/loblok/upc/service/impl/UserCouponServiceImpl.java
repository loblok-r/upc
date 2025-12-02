package cn.loblok.upc.service.impl;

import cn.loblok.upc.entity.CouponTemplate;
import cn.loblok.upc.entity.UserCoupon;
import cn.loblok.upc.enums.CouponStatus;
import cn.loblok.upc.exception.BizException;
import cn.loblok.upc.mapper.UserCouponMapper;
import cn.loblok.upc.service.CouponTemplateService;
import cn.loblok.upc.service.UserCouponService;
import cn.loblok.upc.util.RedisUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {

    @Autowired
    private CouponTemplateService couponTemplateService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 2. 用 Redisson 做分布式锁（新功能）
    @Autowired
    private RedissonClient redissonClient;

    // Lua 脚本（可提取为常量或 resource 文件）
    private static final String DEDUCT_STOCK_LUA_SCRIPT =
            "local stock = redis.call('GET', KEYS[1])\n" +
                    "if not stock then\n" +
                    "    return 0\n" +
                    "end\n" +
                    "if tonumber(stock) <= tonumber(ARGV[1]) then\n" +
                    "    return 0\n" +
                    "end\n" +
                    "redis.call('DECR', KEYS[1])\n" +
                    "return 1";

    @Override
    public void grantCoupon(Long userId, String templateCode) {
        // 1. 查模板
        CouponTemplate template = couponTemplateService.getByCode(templateCode);
        if (template == null || template.getStatus() != 1) {
            throw new BizException("优惠券不可用");
        }

        // 🔑 构造锁 key：用户 + 模板
        String lockKey = "coupon:lock:" + userId + ":" + template.getId();

        // 使用 Redisson 的分布式锁（自动续期、可重入、安全）
        RLock lock = redissonClient.getLock(lockKey);

        try {

            boolean locked = lock.tryLock(1, 10, TimeUnit.SECONDS); // 等待1秒，持有5秒

            if (!locked) {
                throw new BizException("操作频繁，请勿重复点击");
            }

            // 🔄 执行核心发放逻辑（带事务）
            doGrantCoupon(userId, template);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("获取锁被中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock(); // Redisson 会自动处理
            }
        }

    }

    /**
     * 实际发放逻辑（带事务）
     */
    @Transactional(rollbackFor = Exception.class)
    protected void doGrantCoupon(Long userId, CouponTemplate template) {
        // 2. 检查用户是否已达领取上限（现在在锁内，安全！）
        //典型的 应用层计数检查，而且因为在外层有 Redisson 分布式锁，所以这个 count + insert 是逻辑原子的，完全安全。
        long receivedCount = this.count(
                new QueryWrapper<UserCoupon>()
                        .eq("user_id", userId)
                        .eq("template_id", template.getId())
        );
        if (receivedCount >= template.getPerUserLimit()) {
            throw new BizException("已达领取上限");
        }

        boolean stockDeducted = false;
        try {
            // 3. Redis 扣减库存（Lua 脚本）
            if (!redisCouponStockDeduct(template.getId())) {
                throw new BizException("优惠券已抢光");
            }
            stockDeducted = true;

            // 4. 创建用户券实例
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUserId(userId);
            userCoupon.setTemplateId(template.getId());
            userCoupon.setTemplateCode(template.getTemplateCode());
            userCoupon.setStatus(CouponStatus.UNUSED.getValue());
            userCoupon.setReceivedAt(LocalDateTime.now());
            userCoupon.setExpiredAt(calculateExpireTime(template));

            // 5. 保存到 DB
            this.save(userCoupon);

            // 6. 更新缓存
            updateUserCouponCache(userId);

        } catch (Exception e) {
            // 补偿 Redis 库存
            if (stockDeducted) {
                try {
                    redisTemplate.opsForValue().increment(RedisUtils.buildCouponStockKey(template.getId()), 1);
                    log.warn("DB save failed, compensated Redis stock for template: {}", template.getId());
                } catch (Exception ex) {
                    log.error("Failed to compensate Redis stock", ex);
                }
            }
            throw e;
        }
    }

    /**
     * 原子扣减优惠券模板库存
     * @param templateId 优惠券模板ID
     * @return true=扣减成功，false=库存不足
     */
    private boolean redisCouponStockDeduct(Long templateId) {
        String stockKey = RedisUtils.buildCouponStockKey(templateId);
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(DEDUCT_STOCK_LUA_SCRIPT);
        redisScript.setResultType(Long.class);

        // 执行脚本：KEYS=[stockKey], ARGV=[0]
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(stockKey), "0");
        return result != null && result == 1L;
    }

    // 计算优惠券过期时间
    private LocalDateTime calculateExpireTime(CouponTemplate template) {
        LocalDateTime now = LocalDateTime.now();

        if (template.getValidDays() != null && template.getValidDays() > 0) {
            return now.plusDays(template.getValidDays());
        }

        if (template.getFixedValidTo() != null) {
            return template.getFixedValidTo();
        }

        log.warn("Coupon template [{}] has no valid config, using default 30 days.", template.getId());
        return now.plusDays(30);
    }

    // 更新用户券缓存
    private void updateUserCouponCache(Long userId) {
        String cacheKey = "coupon:user:" + userId + ":unused";
        // 查询用户所有未使用的券（可分页/限数量）
        List<UserCoupon> unusedCoupons = this.lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, CouponStatus.UNUSED.getValue())
                .list();

        // 序列化后缓存（例如 JSON）
        String json = JSON.toJSONString(unusedCoupons);
        redisTemplate.opsForValue().set(cacheKey, json, Duration.ofMinutes(10));
    }
}