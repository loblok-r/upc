package cn.loblok.upc.service.impl;


import cn.loblok.upc.client.DeepSeekClient;
import cn.loblok.upc.dto.ChatBotRequest;
import cn.loblok.upc.service.ChatService;
import cn.loblok.upc.service.assist.KnowledgeLoader;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {


    private final KnowledgeLoader knowledgeLoader;


    private final DeepSeekClient deepSeekClient;

    @Override
    public String generateAnswer(String userMessage, List<ChatBotRequest.MessageContext>  history) {
        // 1. 获取知识库 JSON
        JsonNode knowledge = knowledgeLoader.getKnowledgeJson();

        // 2. 构造系统提示词（System Prompt）
        String systemPrompt = """
            你是一个名为 "Mitce AI" 的专业客服助手。
            你的任务是根据以下【背景知识】回答用户的问题。

            【重要规则】
            1. 只能根据【背景知识】回答问题。如果知识库中没有相关信息，必须回答：“抱歉，我不清楚这个具体细节，建议您查看帮助文档或联系人工客服。”
            2. 严禁编造事实（Hallucination）。
            3. 语气要专业、亲切、简洁。
            4. 货币单位默认为美元(USD)。

            【背景知识 (JSON Context)】
            %s
            """.formatted(knowledge.toPrettyString());

        // 3. 调用 DeepSeek
        return deepSeekClient.callDeepSeek(systemPrompt, objectMapper.valueToTree(history), userMessage);
    }

    // 手动注入 ObjectMapper（避免循环依赖）
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
}