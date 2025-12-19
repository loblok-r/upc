package cn.loblok.upc.modules.mall.dto;


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
    private Integer balance;        // 当前积分余额
    private Integer totalEarned;    // 累计获得积分
    private Integer totalSpent;     // 累计消费积分
}