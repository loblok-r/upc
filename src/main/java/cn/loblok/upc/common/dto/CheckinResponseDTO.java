package cn.loblok.upc.common.dto;

import lombok.Data;

//签到响应体
@Data
public class CheckinResponseDTO {

    private Integer points;
    private Integer streakDays;

    public CheckinResponseDTO(Integer points, Integer streakDays){
        this.points = points;
        this.streakDays =streakDays;
    }
}