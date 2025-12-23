package cn.loblok.upc.trade.dto.mall;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀活动信息
 */
@Data
public class FlashSaleDTO {
    private String id;
    private String productId;
    private String productName;
    private Integer salePrice;
    private Integer originalPrice;
    private Integer totalStock;
    private Integer remainingStock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime createdAt;
}