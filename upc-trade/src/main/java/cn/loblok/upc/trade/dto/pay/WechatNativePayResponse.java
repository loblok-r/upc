package cn.loblok.upc.trade.dto.pay;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 微信扫码支付响应
 */
@Data
@AllArgsConstructor
public class WechatNativePayResponse {
    private String codeUrl; // 唯一需要的字段
}