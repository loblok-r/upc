package cn.loblok.upc.ai.client;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
@Slf4j
public class QwenClient {

    @Value("${ai.qwen.api-key:}")
    private String apiKey;

    @Value("${ai.qwen.text-model:qwen-turbo}")
    private String textModelName;

    @Value("${ai.qwen.base-url:https://dashscope.aliyuncs.com/api/v1}")
    private String baseUrl;

    private Generation generation;


    @PostConstruct
    public void init() {
        log.info("解密后的 API Key 长度为: {}", (apiKey != null ? apiKey.length() : 0));
        log.info("API Key 内容前缀: {}", (apiKey != null && apiKey.length() > 5 ? apiKey.substring(0, 5) : "无效"));
        this.generation = new Generation();
    }

    /**
     * 生成文本内容
     *
     * @param prompt 用户输入的提示词
     * @return 生成的文本内容
     * @throws NoApiKeyException
     * @throws InputRequiredException
     */
    public String generateText(String prompt) throws NoApiKeyException, InputRequiredException {
        try {
            GenerationParam param = GenerationParam.builder()
                    .model(textModelName)
                    .messages(Collections.singletonList(Message.builder().role(Role.USER.getValue()).content(prompt).build()))
                    .apiKey(apiKey)
                    .build();

            return generation.call(param).getOutput().getText();
        } catch (ApiException e) {
            throw new RuntimeException("调用AI文本生成接口异常: " + e.getMessage(), e);
        }
    }

//    /**
//     * 生成图像
//     *
//     * @param prompt   用户输入的提示词
//     * @param refImage 参考图像URL（可为空）
//     * @return 生成的图像URL
//     * @throws NoApiKeyException
//     * @throws InputRequiredException
//     */
//    public AiResult generateImage(Long userId, String prompt, String refImage) throws NoApiKeyException, InputRequiredException {
//        log.info("进入generateImage 方法");
//
//        log.info("qwen-apikey:", apiKey);
//        log.info("qwen-text-model:", textModelName);
//        log.info("qwen-image-model:", imageModelName);
//        log.info("qwen-base-url:", baseUrl);
//        try {
//            ImageSynthesisParam param;
//            if (refImage != null && !refImage.isEmpty()) {
//                // 如果提供了参考图像，则使用图像到图像的模式
//                Map<String, Object> parameters = new HashMap<>();
//                parameters.put("reference_image", refImage);
//
//                param = ImageSynthesisParam.builder()
//                        .model(imageModelName)
//                        .prompt(prompt)
//                        .n(1)
//                        .size("1024*1024")
//                        .apiKey(apiKey)
//                        .parameters(parameters)
//                        .build();
//            } else {
//                // 文本到图像模式
//                param = ImageSynthesisParam.builder()
//                        .model(imageModelName)
//                        .prompt(prompt)
//                        .n(1)
//                        .size("1024*1024")
//                        .apiKey(apiKey)
//                        .build();
//            }
//
//
//            // 获取远程图片 URL
//            log.info("调用AI图像生成接口");
//            Map<String, String> result = imageSynthesis.call(param).getOutput().getResults().get(0);
//            log.info("调用AI图像生成接口 结束");
//            String remoteImageUrl = result.get("url");
//
//            if (remoteImageUrl == null || remoteImageUrl.isEmpty()) {
//                throw new RuntimeException("AI 返回的图片 URL 为空");
//            }
//
//            log.info("下载并上传到腾讯云 COS,返回临时签名 URL");
//            // === 下载并上传到腾讯云 COS，返回临时签名 URL ===
//            try (InputStream in = new URL(remoteImageUrl).openStream()) {
//                // 上传到 COS 并生成 30 分钟有效的签名 URL
//                return tencentCOSUtil.uploadAndGenerateSignedUrl(userId,in, "generated.jpg", 30);
//            } catch (IOException e) {
//                throw new RuntimeException("下载或上传图片失败: " + e.getMessage(), e);
//            }
//
//        } catch (Exception e) {
//            log.error("AI图像生成核心错误：", e);
//            throw new RuntimeException("调用AI图像生成接口异常: " + e.getMessage(), e);
//        }
//    }
}