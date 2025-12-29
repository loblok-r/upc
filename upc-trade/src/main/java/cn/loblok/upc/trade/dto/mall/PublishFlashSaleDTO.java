package cn.loblok.upc.trade.dto.mall;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublishFlashSaleDTO {
    private String id;
    private Integer totalStock;
    private LocalDateTime endTime;
}