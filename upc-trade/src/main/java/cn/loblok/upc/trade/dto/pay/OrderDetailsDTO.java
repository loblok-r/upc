package cn.loblok.upc.trade.dto.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetailsDTO {
    @Schema(description = "订单ID")
    private String id;          // 套餐ID (如 "week", "month")
    @Schema(description = "订单名称")
    private String planName;    // 套餐名称

    @Schema(description = "订单价格")
    // 金额一定要用 BigDecimal，不能用 Double/Float，避免精度丢失
    private BigDecimal price;

    @Schema(description = "订单原价")
    // 可选字段，使用包装类型 BigDecimal 允许为 null
    private BigDecimal originalPrice;

    @Schema(description = "订单货币")
    private String currency;    // 货币符号 (如 "￥")
    @Schema(description = "计费描述")
    private String billingText; // 计费描述
    @Schema(description = "订单周期")
    private String period;      // 周期 (如 "月", "年")

    // 可选字段
    @Schema(description = "角标")
    private String badge;       // 角标 (如 "省 34%")
    @Schema(description = "是否热门")
    private Boolean isPopular;  // 是否热门
}