package cn.loblok.upc.service;

public interface UserEntitlementService {

    // 检查是否可使用某套餐（综合：等级 + 会员 + 体验期 + 优惠券）
    boolean canUsePlan(Long userId, String plan);

    // 扣减一次使用（日常额度 -1 或 消耗一张券）
    void consumeQuota(Long userId, String plan);
}
