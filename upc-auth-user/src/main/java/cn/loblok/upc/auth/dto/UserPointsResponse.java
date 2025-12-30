package cn.loblok.upc.auth.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户积分响应体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointsResponse {

    /**
     * 积分余额
     */
    @Schema(description = "积分余额")
    private Integer balance;

    /**
     * 累计收入积分
     */
    @Schema(description = "累计收入积分")
    private Integer totalEarned;

    /**
     * 累计支出积分
     */
    @Schema(description = "累计支出积分")
    private Integer totalSpent;
}