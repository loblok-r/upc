package cn.loblok.upc.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExpTransactionDTO {
    private Long id;
    private String bizType;
    private String bizTypeDesc;
    private Integer deltaExps;
    private Long balanceAfter;
    private LocalDateTime createdAt;
}