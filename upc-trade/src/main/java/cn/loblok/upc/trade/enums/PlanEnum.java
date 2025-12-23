package cn.loblok.upc.trade.enums;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 枚举类：会员计划
 */
@Getter
public enum PlanEnum {
    WEEK("week","7天体验卡",7,new BigDecimal("5.99"),new BigDecimal("5.99"),"7天后自动续费 (￥19.99/月)"),
    MONTH("month","月度会员",30,new BigDecimal("19.99"),new BigDecimal("29.99"),"下个月自动续费"),
    YEAR("year","年度会员",365,new BigDecimal("7.9"),new BigDecimal("19.99"),"下个月自动续费");

    private final String value;
    private final String name;
    private final int days;
    private final BigDecimal price;
    private final BigDecimal originalPrice;
    private final String discription;

    PlanEnum(String value, String name, int days, BigDecimal price, BigDecimal originalPrice, String discription) {
        this.value = value;
        this.name = name;
        this.days = days;
        this.price = price;
        this.originalPrice = originalPrice;
        this.discription = discription;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public int getDays() {
        return days;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public String getDiscription() {
        return discription;
    }

    /**
     * 核心方法：根据前端传来的 planId (value) 查找对应的枚举
     */
    public static PlanEnum getByValue(String value) {
        return Arrays.stream(values())
                .filter(plan -> plan.getValue().equals(value))
                .findFirst()
                .orElse(null); // 或者抛出自定义异常
    }
}