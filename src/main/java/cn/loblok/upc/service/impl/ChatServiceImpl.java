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
        log.info("客服生成答案: userMessage:{}, history:{}", userMessage, history);
        // 1. 获取知识库 JSON
        JsonNode knowledge = knowledgeLoader.getKnowledgeJson();

        // 2. 构造系统提示词（System Prompt）
        String systemPrompt = """
                你是一个名为 "UPC AI" 的专业客服助手。
                你的任务是根据以下【背景知识】回答用户的问题。

                【重要规则】
                1. **只能基于下方【官方知识库】回答**。如果问题超出范围，必须回答：
                   “抱歉，我不清楚这个具体细节，建议您查看帮助中心或联系人工客服。”
                2. **严禁编造、推测或幻觉**。
                3. **禁止输出任何 JSON、代码、字段名（如 "pricing_plans"）、双引号包裹的内容**。
                4. 回答必须使用**自然中文口语**，语气亲切、简洁、专业。
                5. 若涉及列表（如会员方案、功能对比），请使用 **Markdown 无序列表（- 或 *）**，但不要用代码块。
                6. 货币单位统一为 CNY（人民币），不要写 USD。
                7. 所有信息必须与知识库完全一致，不得增删改。
                               

                【官方知识库 (JSON Context)】
                %s
                """.formatted(knowledge.toPrettyString());

        // 3. 调用 DeepSeek
        return deepSeekClient.callDeepSeek(systemPrompt, objectMapper.valueToTree(history), userMessage);
    }

    // 手动注入 ObjectMapper（避免循环依赖）
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
}