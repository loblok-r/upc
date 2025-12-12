package cn.loblok.upc.dto;


import lombok.Data;

@Data
public class ExchangeProducesRequest {

    private String productId;
    private int quantity;
}