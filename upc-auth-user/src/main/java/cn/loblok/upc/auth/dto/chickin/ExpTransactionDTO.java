package cn.loblok.upc.auth.dto.chickin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 *  经验值记录
 */
@Data
public class ExpTransactionDTO {
    /**
     * 经验值记录ID
     */
    @Schema(description = "经验值记录ID")
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
     * 增减的经验值
     */
    @Schema(description = "增减的经验值")
    private Integer deltaExps;
    /**
     * 总经验值
     */
    @Schema(description = "总经验值")
    private Long balanceAfter;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}