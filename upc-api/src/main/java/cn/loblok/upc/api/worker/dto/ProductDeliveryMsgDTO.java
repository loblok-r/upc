package cn.loblok.upc.api.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDeliveryMsgDTO implements Serializable {
    private Long orderId;      // 订单ID (回填状态用)
    private Long userId;        // 谁买的
    private String category;    // 商品分类 (VIRTUAL, VOUCHER, PHYSICAL)
    private String productName; // 商品名称 (通知用)
    private Integer pointsSpent;
    private String source;
    private String deliveryConfig; // 核心：商品的配置信息 (JSON 格式，如 {"type":"compute_power", "amount":100})
}