package cn.loblok.upc.modules.mall.dto;


import lombok.Data;

@Data
public class ExchangeProducesRequest {

    private String productId;
    private int quantity;
}