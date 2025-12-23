package cn.loblok.upc.auth.dto.chickin;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 积分交易记录
 */
@Data
public class PointTransactionDTO {
    private Long id;
    private String bizType;
    private String bizTypeDesc;
    private Integer deltaPoints;
    private Long balanceAfter;
    private LocalDateTime createdAt;
}