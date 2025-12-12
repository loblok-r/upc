package cn.loblok.upc.service;

import cn.loblok.upc.dto.ChatBotRequest;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 生成回复
     * @param userMessage 用户输入
     * @param history 上下文
     * @return 回复
     */
    String generateAnswer(String userMessage, List<ChatBotRequest.MessageContext> history);
}
