package cn.loblok.upc.trade.dto.mall;


import lombok.Data;

@Data
public class ExchangeProducesRequest {

    private String productId;
    private int quantity;
}