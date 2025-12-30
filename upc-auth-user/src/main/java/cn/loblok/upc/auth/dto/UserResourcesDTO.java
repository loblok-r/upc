package cn.loblok.upc.auth.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户资源信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResourcesDTO {
    @Schema(description = "今日使用情况")
    private DailyUsageDTO dailyUsage;
    @Schema(description = "可用计算资源")
    private int computingPower;
    @Schema(description = "最大可用计算资源")
    private int maxComputingPower;
}