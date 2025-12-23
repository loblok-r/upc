package cn.loblok.upc.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "upc.storage")
public class StorageProperties {
    /** 存储类型: cos 或 minio */
    private String type = "cos";
    /** 腾讯云前缀 */
    private String cosPrefix;
    /** MinIO前缀 */
    private String minioPrefix;
}