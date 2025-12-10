package cn.loblok.upc.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品信息
 */
@Data
public class ProductDTO {
    private String id;
    private String name;
    private String description;
    private String category;
    private Integer pointsRequired;
    private BigDecimal originalPrice;
    private Integer stock;
    private String tag;
    private String imageUrl;
    private LocalDateTime createdAt;
}