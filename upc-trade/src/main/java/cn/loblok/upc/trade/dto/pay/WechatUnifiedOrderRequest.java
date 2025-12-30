package cn.loblok.upc.trade.dto.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 微信统一下单请求参数
 */
@Data
@Builder
public class WechatUnifiedOrderRequest {
    @Schema(description = "商户订单号")
    private String outTradeNo;     // 商户订单号
    @Schema(description = "总金额")
    private Integer totalFee;      // 总金额，单位为分
    @Schema(description = "商品描述")
    private String description;    // 商品描述
    @Schema(description = "终端IP")
    private String spbillCreateIp;// 终端IP
    @Schema(description = "回调地址")
    private String notifyUrl;      // 通知地址
    @Schema(description = "交易类型")
    private String tradeType;      // 交易类型
    @Schema(description = "用户标识")
    private String openid;         // 用户标识
}