package cn.loblok.upc.ai.service.impl;

import cn.hutool.core.lang.UUID;
import cn.loblok.upc.ai.dto.AiResult;
import cn.loblok.upc.ai.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;

@Service
@Slf4j
// 只有当配置为 minio 时才加载此 Bean
@ConditionalOnProperty(name = "upc.storage.type", havingValue = "minio")
public class MinioStorageServiceImpl implements FileStorageService {

    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;
    @Value("${minio.bucketName}")
    private String bucketName;

    // 定义为成员变量，不用每次请求都 new 一个新的
    private MinioClient minioClient;

    // 在使用前初始化（或者使用 @PostConstruct）
    private synchronized MinioClient getMinioClient() {
        if (minioClient == null) {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        }
        return minioClient;
    }

    @Override
    public AiResult uploadImage(Long userId, InputStream inputStream, String originalFilename, int expireMinutes) {

        log.info("使用本地 MinIO 上传...");
        try {// 获取客户端
             MinioClient client = getMinioClient();

            // 生成文件名
            String objectKey = "ai_images/user_" + userId + "/" + UUID.randomUUID() + "_" + originalFilename;

            // 上传
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(inputStream, -1, 10485760) // 10MB part size
                    .contentType("image/jpeg")
                    .build());

            // 生成访问 URL (如果是私有读，需要生成带签名的 URL)
            String url = endpoint + "/" + bucketName + "/" + objectKey;

            return new AiResult(null, url, url);
        } catch (Exception e) {
            throw new RuntimeException("MinIO 上传失败", e);
        }
    }
}