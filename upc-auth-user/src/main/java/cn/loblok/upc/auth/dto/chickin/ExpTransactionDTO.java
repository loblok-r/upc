package cn.loblok.upc.auth.dto.chickin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 *  经验值记录
 */
@Data
public class ExpTransactionDTO {
    private Long id;
    private String bizType;
    private String bizTypeDesc;
    private Integer deltaExps;
    private Long balanceAfter;
    private LocalDateTime createdAt;
}