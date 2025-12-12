package cn.loblok.upc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DeepSeekClient {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.model}")
    private String model;

    @Value("${deepseek.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String callDeepSeek(String systemPrompt, JsonNode history, String userMessage) {
        try {
            // 构建 messages 数组
            ArrayNode messages = objectMapper.createArrayNode();
            messages.add(createMessage("system", systemPrompt));

            // 添加历史记录（history 是前端传来的 List<MessageContext>）
            if (history.isArray()) {
                for (JsonNode msg : history) {
                    String role = msg.get("role").asText();
                    String content = msg.get("content").asText();
                    messages.add(createMessage(role, content));
                }
            }

            // 添加当前用户消息
            messages.add(createMessage("user", userMessage));

            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.set("messages", messages);
            requestBody.put("temperature", 0.3);

            // 设置 Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(
                    requestBody.toString(),
                    headers
            );

            // 发送请求
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    baseUrl,
                    request,
                    JsonNode.class
            );

            // 提取回复内容
            JsonNode responseBody = response.getBody();
            if (responseBody != null && responseBody.has("choices")) {
                return responseBody.get("choices")
                        .get(0)
                        .get("message")
                        .get("content")
                        .asText()
                        .trim();
            }

            return "抱歉，服务暂时不可用。";

        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，处理请求时发生错误：" + e.getMessage();
        }
    }

    private ObjectNode createMessage(String role, String content) {
        ObjectNode msg = objectMapper.createObjectNode();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }
}