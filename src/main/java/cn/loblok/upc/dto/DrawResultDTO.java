package cn.loblok.upc.dto;

import lombok.Data;

@Data
public class DrawResultDTO {
    private String prizeId;      // 中奖奖品ID
    private UserDrawInfo user;   // 更新后的用户简要信息
}