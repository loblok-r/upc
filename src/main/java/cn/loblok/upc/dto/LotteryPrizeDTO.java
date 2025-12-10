package cn.loblok.upc.dto;

import lombok.Data;

/**
 * 抽奖奖品
 */
@Data
public class LotteryPrizeDTO {
    private String id;
    private String title;      // 对应 name
    private String subtitle;   // 对应 description
    private String icon;       // 对应 icon_name
    private String color;      // 对应 display_color
    private String badge;      // 对应 tag
}