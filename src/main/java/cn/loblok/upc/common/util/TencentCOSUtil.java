package cn.loblok.upc.common.util;

import cn.loblok.upc.modules.ai.dto.AiResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;


@Service
@Slf4j
public class TencentCOSUtil {


    // 替换为你的实际配置
    @Value("${tencent.cos.secret-id}")
    private String secretId;
    
    @Value("${tencent.cos.secret-key}")
    private  String secretKey;
    
    @Value("${tencent.cos.bucket-name}")
    private  String bucketName;
    
    @Value("${tencent.cos.region}")
    private  String region;

    /**
     * 上传图片并返回临时可访问的签名 URL
     *
     * @param inputStream 图片数据流
     * @param originalFilename 原始文件名（用于扩展名，如 "cat.png"）
     * @param expireMinutes 签名 URL 有效时间（分钟）
     * @return 临时可访问的 HTTPS URL
     */
    public AiResult uploadAndGenerateSignedUrl(Long userId, InputStream inputStream, String originalFilename, int expireMinutes) {
        log.info("uploadAndGenerateSignedUrl 开始执行：");
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            log.info("生成唯一文件名（保留扩展名）");

            String objectKey = buildCosKey(userId, originalFilename);

            String public_url = buildPublicUrl(bucketName, region,objectKey);

            log.info("上传对象");
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, inputStream, null);
            cosClient.putObject(putObjectRequest);

            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey);
            urlRequest.setExpiration(new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000L));
            URL signedUrl = cosClient.generatePresignedUrl(urlRequest);

            String tmpImgUrl = signedUrl.toString();
            log.info("临时图片URL：{}", tmpImgUrl);
            return new AiResult(null, public_url,public_url);

        } catch (CosClientException e) {
            throw new RuntimeException("COS 上传失败: " + e.getMessage(), e);
        } finally {
            cosClient.shutdown(); // 关闭客户端
        }
    }

    private String buildPublicUrl(String bucketName, String region, String objectKey) {
        return "https://"+bucketName+".cos."+region+".myqcloud.com/"+objectKey;
    }

    public String getTmpImageUrl(String objectKey, int expireMinutes) {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        COSClient cosClient = new COSClient(cred, clientConfig);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey);
        request.setExpiration(new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000)); // 30分钟后过期
        URL url = cosClient.generatePresignedUrl(request);
        return url.toString();
    }

    public static String buildCosKey(Long userId, String originalFilename){
        String ext = "";
        if (originalFilename != null && originalFilename.lastIndexOf('.') > 0) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return String.format("ai_images/user_%d/%s_%s%s", userId, timestamp, uuid, ext);
    }
}