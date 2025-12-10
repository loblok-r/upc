package cn.loblok.upc.dto;

import lombok.Data;

/**
 * 签到响应参数
 */
@Data
public class CheckinResponseDTO {

    private Integer points;
    private Integer streakDays;

    public CheckinResponseDTO(Integer points, Integer streakDays){
        this.points = points;
        this.streakDays =streakDays;
    }
}