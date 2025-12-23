package cn.loblok.upc.common.utils;

import cn.loblok.upc.common.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ImageUtil {

    @Autowired
    private StorageProperties storageProperties;

    private static ImageUtil instance;

    @PostConstruct
    public void init() {
        instance = this;
    }

    // 基础缩略图：256宽，webp格式
    private static final String THUMBNAIL_SUFFIX = "?imageMogr2/thumbnail/256x/format/webp";
    private static final String DETAIL_SUFFIX = "?imageMogr2/format/webp";

    public static String getOptimizedUrl(String originalUrl, boolean isThumbnail) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            return "";
        }

        String finalUrl = originalUrl;

        // 如果在本地环境切换到了 MinIO
        if (instance != null && "minio".equalsIgnoreCase(instance.storageProperties.getType())) {
            String cosPrefix = instance.storageProperties.getCosPrefix();
            String minioPrefix = instance.storageProperties.getMinioPrefix();

            // 如果库里存的是 COS 地址，动态替换为 MinIO 地址
            if (cosPrefix != null && finalUrl.startsWith(cosPrefix)) {
                finalUrl = finalUrl.replace(cosPrefix, minioPrefix);
            }
        }

        if (finalUrl.contains("?")) {
            return finalUrl;
        }
        return finalUrl + (isThumbnail ? THUMBNAIL_SUFFIX : DETAIL_SUFFIX);
    }
}