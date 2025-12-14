package cn.loblok.upc.config;

// import com.wechat.pay.java.core.Config;
// import com.wechat.pay.java.core.RSAAutoCertificateConfig;
// import com.wechat.pay.java.service.payments.nativepay.NativePayService;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

/**
 * 微信支付配置类
 * 暂时注释掉所有内容，等需要时再启用
 */
// @Configuration
public class MyWechatPayConfig {

    // 暂时不创建任何Bean
    /*
    @Value("${wechat.pay.private-key-path}")
    private String privateKeyPath;

    @Value("${wechat.pay.cert-path}")
    private String certPath;

    @Value("${wechat.pay.mch-id}")
    private String merchantId;

    @Bean
    public Config wechatPayConfig() {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(merchantId)
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(certPath)
                .build();
    }

    @Bean
    public NativePayService nativePayService(Config wechatPayConfig) {
        return new NativePayService.Builder().config(wechatPayConfig).build();
    }
    */
}