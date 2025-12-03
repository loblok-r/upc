package cn.loblok.upc.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PointTransactionDTO {
    private Long id;
    private String bizType;
    private String bizTypeDesc;
    private Integer deltaPoints;
    private Long balanceAfter;
    private LocalDateTime createdAt;
}