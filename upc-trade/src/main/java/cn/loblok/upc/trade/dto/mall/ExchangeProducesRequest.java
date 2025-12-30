package cn.loblok.upc.trade.dto.mall;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ExchangeProducesRequest {

    @Schema(description = "商品ID")
    private String productId;
    @Schema(description = "商品数量")
    private int quantity;
}