package cn.loblok.upc.api.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeckillOrderCreateDTO {
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 抢购活动ID
     */
    private String flashSaleId;
    /**
     * 抢购商品ID
     */
    private String productId;
    /**
     * 抢购时的积分价格
     */
    private int payPoints;

    /**
     * 链路追踪ID，用于对账和幂等
     */
    private String traceId;
}