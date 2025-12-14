package cn.loblok.upc.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信支付配置类
 */
@Configuration
public class WechatPayConfig {

    @Value("${wechat.pay.private-key-path}")
    private String privateKeyPath;

    @Value("${wechat.pay.cert-path}")
    private String certPath;

    @Value("${wechat.pay.mch-id}")
    private String merchantId;

    /**
     * 创建微信支付配置实例
     */
    @Bean
    public Config wechatPayConfig() {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(merchantId)
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(certPath)
                .build();
    }

    /**
     * 创建微信 Native 支付服务实例
     */
    @Bean
    public NativePayService nativePayService(Config wechatPayConfig) {
        return new NativePayService.Builder().config(wechatPayConfig).build();
    }
}