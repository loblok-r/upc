package cn.loblok.upc.trade.dto.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublishFlashSaleDTO {
    @Schema(description = "秒杀活动ID")
    private String id;
    @Schema(description = "总库存")
    private Integer totalStock;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}