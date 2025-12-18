package cn.loblok.upc.client;

import cn.loblok.upc.dto.AiResult;
import cn.loblok.upc.util.TencentCOSUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SilionClient {

    @Value("${ai.silicon.api-key}")
    private String apiKey;

    @Value("${ai.silicon.image-model}")
    private String imageModel;

    @Value("${ai.silicon.base-url}")
    private String baseUrl;

    @Autowired
    private TencentCOSUtil tencentCOSUtil;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 使用 SiliconFlow 生成图像
     */
    public AiResult generateImage(Long userId, String prompt, String refImage) {
        log.info("开始调用 SiliconFlow 图像生成接口, 模型: {}", imageModel);

        try {
            // 构造请求体 (兼容 OpenAI 格式)
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", imageModel);
            requestBody.put("prompt", prompt);
            requestBody.put("batch_size", 1); // 相当于之前的 n(1)
            requestBody.put("image_size", "1024x1024");

            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url(baseUrl + "/images/generations")
                    .post(RequestBody.create(jsonPayload, MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + apiKey) // 必须以 Bearer 开头
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    log.error("SiliconFlow 调用失败: {}", errorBody);
                    throw new RuntimeException("API 返回错误: " + errorBody);
                }

                // 解析响应获取图片 URL
                JsonNode root = objectMapper.readTree(response.body().string());
                String remoteImageUrl = root.path("images").get(0).path("url").asText();

                log.info("生成成功，正在转存至腾讯云 COS...");
                try (InputStream in = new URL(remoteImageUrl).openStream()) {
                    return tencentCOSUtil.uploadAndGenerateSignedUrl(userId, in, "sf_generated.jpg", 30);
                }
            }
        } catch (Exception e) {
            log.error("SiliconFlow 图像生成核心错误：", e);
            throw new RuntimeException("调用图像生成接口异常: " + e.getMessage());
        }
    }
}
