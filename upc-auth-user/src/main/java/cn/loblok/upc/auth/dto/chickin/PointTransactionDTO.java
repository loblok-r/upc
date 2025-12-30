package cn.loblok.upc.auth.dto.chickin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 积分交易记录
 */
@Data
public class PointTransactionDTO {
    /**
     * 交易ID
     */
    @Schema(description = "交易ID")
    private Long id;
    /**
     * 业务类型
     */
    @Schema(description = "业务类型")
    private String bizType;
    /**
     * 业务类型描述
     */
    @Schema(description = "业务类型描述")
    private String bizTypeDesc;
    /**
     * 积分变化量
     */
    @Schema(description = "积分变化量")
    private Integer deltaPoints;
    /**
     * 积分余额
     */
    @Schema(description = "积分余额")
    private Long balanceAfter;
    /**
     * 交易时间
     */
    @Schema(description = "交易时间")
    private LocalDateTime createdAt;
}