package cn.loblok.upc.auth.dto.chickin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 签到响应参数
 */
@Data
public class CheckinResponseDTO {

    /**
     * 签到积分
     */
    @Schema(description = "签到积分")
    private Integer points;

    /**
     * 连续签到天数
     */
    @Schema(description = "连续签到天数")
    private Integer streakDays;

    public CheckinResponseDTO(Integer points, Integer streakDays){
        this.points = points;
        this.streakDays =streakDays;
    }
}