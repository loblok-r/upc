package cn.loblok.upc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建会员订单请求参数
 */
@Data
public class CreateMembershipOrderRequest {
    /**
     * 会员类型：MONTHLY / PERMANENT
     */
    @NotBlank(message = "会员类型不能为空")
    private String membershipType;

    /**
     * 支付渠道（可选）：wechat / alipay
     * MVP 阶段可忽略，后端默认 wechat
     */
    private String payChannel = "wechat";
}