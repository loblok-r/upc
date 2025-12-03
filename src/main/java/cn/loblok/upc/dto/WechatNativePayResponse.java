package cn.loblok.upc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// 专用于 Native 扫码支付
@Data
@AllArgsConstructor
public class WechatNativePayResponse {
    private String codeUrl; // 唯一需要的字段
}