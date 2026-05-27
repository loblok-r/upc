package cn.loblok.upc.auth.service.impl;

import cn.hutool.json.JSONUtil;
import cn.loblok.upc.auth.mapper.UserCouponMapper;
import cn.loblok.upc.auth.dto.CouponResponse;
import cn.loblok.upc.auth.entity.CouponTemplate;
import cn.loblok.upc.auth.entity.IssueContext;
import cn.loblok.upc.auth.entity.UserCoupon;
import cn.loblok.upc.auth.enums.CouponStatus;
import cn.loblok.upc.auth.enums.CouponTemplateType;
import cn.loblok.upc.auth.service.CouponTemplateService;
import cn.loblok.upc.auth.service.UserCouponService;
import cn.loblok.upc.auth.service.UserEntitlementService;
import cn.loblok.upc.common.exception.BizException;
import cn.loblok.upc.common.utils.KeyUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@Primary
@Slf4j
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {

    @Resource
    private CouponTemplateService couponTemplateService;

    @Resource
    private StringRedisTemplate redisTemplate;

    @Resource
    private UserEntitlementService entitlementService;

    // 用 Redisson 做分布式锁（新功能）
    @Resource
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


    //系统事件（注册/升级）自动发放
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantCoupon(Long userId, String templateCode) {
        // 查模板
        CouponTemplate template = couponTemplateService.getByCode(templateCode);
        if (template == null || template.getStatus() != 1) {
            throw new BizException("优惠券不可用");
        }

        //构造锁 key：用户 + 模板
        String lockKey = "coupon:lock:" + userId + ":" + template.getId();

        // 使用 Redisson 的分布式锁（自动续期、可重入、安全）
        RLock lock = redissonClient.getLock(lockKey);

        try {

            boolean locked = lock.tryLock(1, 10, TimeUnit.SECONDS); // 等待1秒，持有5秒

            if (!locked) {
                throw new BizException("操作频繁，请勿重复点击");
            }

            issueCouponInternal(userId, template);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("获取锁被中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void grabLimitedCoupon(Long userId, String activityCode) {
        // 根据活动码获取模板（可配置映射）
        CouponTemplate template = couponTemplateService.getByActivityCode(activityCode);
        if (template == null || template.getStatus() != 1) {
            throw new BizException("活动不存在或已结束");
        }

        // 检查用户今日是否已领取（轻量防重，用 Redis）
        String claimKey = "coupon:claimed:" + activityCode + ":" + LocalDate.now() + ":" + userId;
        if (redisTemplate.hasKey(claimKey)) {
            throw new BizException("您今天已领取过该优惠券");
        }

        // 【关键】获取活动级分布式锁（防超发总量！）
        String lockKey = "coupon:lock:activity:" + activityCode + ":" + LocalDate.now();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(1, 3, TimeUnit.SECONDS)) {
                throw new BizException("系统繁忙，请稍后再试");
            }


            // 在锁内检查库存是否还有
            String stockKey = KeyUtils.buildCouponStockKey(template.getId());
            String stockStr = redisTemplate.opsForValue().get(stockKey);
            long stock = (stockStr != null) ? Long.parseLong(stockStr) : 0L;
            if (stock <= 0) {
                throw new BizException("手慢啦，优惠券已被抢光！");
            }
            // 记录用户已领取（防重复）
            redisTemplate.opsForValue().set(claimKey, "1", Duration.ofDays(1));


            issueCouponInternal(userId, template);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("操作被中断，请稍后重试");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantWelcomeCoupons(Long userId, IssueContext context) {
        // 全局防重（基于 bizId）
        if (redisTemplate.hasKey(context.getBizId())) {
            log.warn("重复发放注册礼包，userId={}", userId);
            return; // 幂等处理
        }

        // 要发放的券模板列表
        List<String> templateCodes = Arrays.asList(
                CouponTemplateType.WELCOME_2025.getTemplateCode(),
                CouponTemplateType.PRO_One_Time_Experience.getTemplateCode()
        );

        // 逐个发放（复用现有 grantCoupon 逻辑）
        for (String code : templateCodes) {
            grantCoupon(userId, code);
        }

        // 标记已发放（24h 过期足够）
        redisTemplate.opsForValue().set(
                context.getBizId(),
                "1",
                Duration.ofHours(24)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean useCoupon(Long userId, Long couponId) {

        UserCoupon coupon = this.getById(couponId);

        // todo 校验：状态、过期、归属

        // 更新券状态为 USED
        boolean updated = this.update(new LambdaUpdateWrapper<UserCoupon>()
                .set(UserCoupon::getStatus, CouponStatus.USED.getValue())
                .set(UserCoupon::getUsedAt, LocalDateTime.now())
                .eq(UserCoupon::getId, couponId)
                .eq(UserCoupon::getStatus, CouponStatus.UNUSED.getValue())
        );

        if (!updated) {
            throw new BizException("操作冲突，请重试");
        }

        //发放权益
        CouponTemplate template = couponTemplateService.getById(coupon.getTemplateId());
        if (template != null && template.getType() == 1) { // type=1 是权益券
            entitlementService.grantEntitlement(userId, template);
        }

        // 清缓存
        updateUserCouponCache(userId);
        return true;
    }

    @Override
    public IPage<CouponResponse> getUserCoupons(Long userId, Integer pageNum, Integer pageSize) {
        log.info("getUserCoupons 查询 userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        Page<UserCoupon> page = new Page<>(pageNum, pageSize);

        QueryWrapper<UserCoupon> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.orderByDesc("created_at");
        IPage<UserCoupon> result = this.page(page, query);
        IPage<CouponResponse> convert = result.convert(item -> {
            CouponResponse couponResponse = new CouponResponse();

            return couponResponse;
        });
        convert.setTotal(result.getTotal());
        convert.setPages(result.getPages());
        return convert;
    }

    /**
     * 实际发放逻辑（带事务）
     */
    // 私有方法，仅在 grantCoupon / grabLimitedCoupon 内部调用
    private void issueCouponInternal(Long userId, CouponTemplate template) {
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
                    redisTemplate.opsForValue().increment(KeyUtils.buildCouponStockKey(template.getId()), 1);
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
        String stockKey = KeyUtils.buildCouponStockKey(templateId);
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(DEDUCT_STOCK_LUA_SCRIPT);
        redisScript.setResultType(Long.class);

        // 执行脚本：KEYS=[stockKey], ARGV=[0]
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(stockKey), "0");
        return result == 1L;
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

        String json = JSONUtil.toJsonStr(unusedCoupons);
        redisTemplate.opsForValue().set(cacheKey, json, Duration.ofMinutes(10));
    }
}