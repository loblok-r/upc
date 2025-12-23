package cn.loblok.upc.api.user.dto;

import lombok.Data;

/**
 * 资产详情（仅自己/内部可见）
 */

@Data
public class UserAssetDTO {
    private Long userId;
    private Integer computingPower; // 算力
    private Integer points;         // 积分
    private Integer lotteryCounts;  // 抽奖次数
    private Integer retroCounts;    // 补签卡
}