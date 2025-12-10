package cn.loblok.upc.config;

/**
 * 微信支付配置
 */
public class WechatPayConfig {
    public static final String APP_ID = "wx1234567890abcdef";      // 公众号/开放平台 AppID
    public static final String MCH_ID = "1900000109";              // 商户号
    public static final String API_KEY = "your_api_key_32_chars";  // API 密钥（32位）
    public static final String NOTIFY_URL = "https://yourdomain.com/api/membership/notify/wechat";
}
