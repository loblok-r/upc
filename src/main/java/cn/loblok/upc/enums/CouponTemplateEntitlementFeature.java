package cn.loblok.upc.enums;

/**
 * 优惠券模板权益功能枚举
 */
public enum CouponTemplateEntitlementFeature {
    HD_ONE_TIME("HD", "hd一次体验券"),
    PRO_ONE_TIME("PRO", "pro一次体验券"),
    MONEDAY("MONEDAY", "一天会员体验券");

    private final String code;
    private final String description;

    CouponTemplateEntitlementFeature(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static CouponTemplateEntitlementFeature fromCode(String code) {
        for (CouponTemplateEntitlementFeature feature : values()) {
            if (feature.code.equals(code)) {
                return feature;
            }
        }
        throw new IllegalArgumentException("未知的权益类型: " + code);
    }

    /**
     * 根据特征码判断是否为功能体验券
     * @return true表示是功能体验券(HD或PRO)
     */
    public boolean isFeatureEntitlement() {
        return this == HD_ONE_TIME || this == PRO_ONE_TIME;
    }

    /**
     * 根据特征码判断是否为会员体验券
     * @return true表示是会员体验券
     */
    public boolean isMembershipEntitlement() {
        return this == MONEDAY;
    }
}