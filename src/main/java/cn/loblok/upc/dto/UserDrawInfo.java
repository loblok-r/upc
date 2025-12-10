package cn.loblok.upc.dto;

import lombok.Data;

/**
 * 用户绘图信息
 */
@Data
public class UserDrawInfo {
    private Long id;
    private Integer lotteryCounts; // 剩余抽奖次数
}