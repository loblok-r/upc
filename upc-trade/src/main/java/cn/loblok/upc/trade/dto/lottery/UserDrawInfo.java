package cn.loblok.upc.trade.dto.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户抽奖信息
 */
@Data
public class UserDrawInfo {
    @Schema(description = "用户ID")
    private Long id;
}