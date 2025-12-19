package cn.loblok.upc.modules.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetailsDTO {
    private String id;          // 套餐ID (如 "week", "month")
    private String planName;    // 套餐名称

    // 金额一定要用 BigDecimal，不能用 Double/Float，避免精度丢失
    private BigDecimal price;

    // 可选字段，使用包装类型 BigDecimal 允许为 null
    private BigDecimal originalPrice;

    private String currency;    // 货币符号 (如 "￥")
    private String billingText; // 计费描述
    private String period;      // 周期 (如 "月", "年")

    // 可选字段
    private String badge;       // 角标 (如 "省 34%")
    private Boolean isPopular;  // 是否热门
}