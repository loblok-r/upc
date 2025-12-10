package cn.loblok.upc.dto;


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
    private DailyUsageDTO dailyUsage;
    private int computingPower;
    private int maxComputingPower;
}