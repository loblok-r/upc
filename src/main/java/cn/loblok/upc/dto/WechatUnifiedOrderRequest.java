package cn.loblok.upc.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 微信统一下单请求参数
 */
@Data
@Builder
public class WechatUnifiedOrderRequest {
    private String outTradeNo;     // 商户订单号
    private Integer totalFee;      // 总金额，单位为分
    private String description;    // 商品描述
    private String spbillCreateIp; // 终端IP
    private String notifyUrl;      // 通知地址
    private String tradeType;      // 交易类型
    private String openid;         // 用户标识
}