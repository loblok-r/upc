package cn.loblok.upc.trade.dto.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 抽奖奖品
 */
@Data
public class FlashOrderRequestDTO {
    @Schema(description = "秒杀活动ID")
    private String flashSaleId;
}