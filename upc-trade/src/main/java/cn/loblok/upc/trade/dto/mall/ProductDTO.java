package cn.loblok.upc.trade.dto.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品信息
 */
@Data
public class ProductDTO {
    @Schema(description = "商品ID")
    private String id;
    @Schema(description = "商品名称")
    private String name;
    @Schema(description = "商品描述")
    private String description;
    @Schema(description = "商品分类")
    private String category;
    @Schema(description = "商品积分")
    private Integer pointsRequired;
    @Schema(description = "商品原价")
    private BigDecimal originalPrice;
    @Schema(description = "商品库存")
    private Integer stock;
    @Schema(description = "商品标签")
    private String tag;
    @Schema(description = "商品图片")
    private String imageUrl;
    @Schema(description = "商品创建时间")
    private LocalDateTime createdAt;
}