package cn.loblok.upc.trade.dto.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀活动信息
 */
@Data
public class FlashSaleDTO {
    @Schema(description = "秒杀活动ID")
    private String id;
    @Schema(description = "商品ID")
    private String productId;
    @Schema(description = "商品名称")
    private String productName;
    @Schema(description = "商品图片")
    private Integer salePrice;
    @Schema(description = "商品原价")
    private Integer originalPrice;
    @Schema(description = "商品总库存")
    private Integer totalStock;
    @Schema(description = "商品剩余库存")
    private Integer remainingStock;
    @Schema(description = "秒杀开始时间")
    private LocalDateTime startTime;
    @Schema(description = "秒杀结束时间")
    private LocalDateTime endTime;
    @Schema(description = "秒杀状态")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}