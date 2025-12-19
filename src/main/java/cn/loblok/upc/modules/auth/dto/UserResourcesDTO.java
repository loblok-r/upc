package cn.loblok.upc.modules.auth.dto;


import cn.loblok.upc.modules.member.dto.DailyUsageDTO;
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