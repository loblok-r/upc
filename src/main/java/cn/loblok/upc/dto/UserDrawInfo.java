package cn.loblok.upc.dto;

import lombok.Data;

@Data
public class UserDrawInfo {
    private Long id;
    private Integer lotteryCounts; // 剩余抽奖次数
}