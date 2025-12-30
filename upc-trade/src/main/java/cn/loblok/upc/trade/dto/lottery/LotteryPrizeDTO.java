package cn.loblok.upc.trade.dto.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 抽奖奖品
 */
@Data
public class LotteryPrizeDTO {
    @Schema(description = "奖品ID")
    private String id;
    @Schema(description = "奖品名称")
    private String title;      // 对应 name
    @Schema(description = "奖品副标题")
    private String subtitle;   // 对应 description
    @Schema(description = "奖品图标")
    private String icon;       // 对应 icon_name
    @Schema(description = "奖品颜色")
    private String color;      // 对应 display_color
    @Schema(description = "奖品标签")
    private String badge;      // 对应 tag
}