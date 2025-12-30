package cn.loblok.upc.trade.dto.lottery;

import cn.loblok.upc.trade.dto.lottery.UserDrawInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 中奖结果
 */
@Data
public class DrawResultDTO {
    @Schema(description = "奖品ID")
    private String prizeId;      // 中奖奖品ID
    @Schema(description = "用户信息")
    private UserDrawInfo user;   // 更新后的用户简要信息
}