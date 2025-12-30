package cn.loblok.upc.trade.dto.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 微信扫码支付响应
 */
@Data
@AllArgsConstructor
public class WechatNativePayResponse {
    @Schema(description = "二维码内容")
    private String codeUrl; // 唯一需要的字段
}