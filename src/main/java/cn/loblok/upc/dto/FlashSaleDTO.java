package cn.loblok.upc.dto;

import lombok.Data;

import java.time.LocalDateTime;

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