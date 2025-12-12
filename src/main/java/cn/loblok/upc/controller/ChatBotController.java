package cn.loblok.upc.controller;

import cn.loblok.upc.dto.ChatBotRequest;
import cn.loblok.upc.dto.ChatBotResponse;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatBotController {


    @Autowired
    private ChatService chatService; // 假设你有一个服务层处理RAG逻辑

    /**
     * 处理对话请求
     * URL: POST /api/chat/completions
     */
    @PostMapping("/completions")
    public Result<ChatBotResponse> chatCompletions(@RequestBody ChatBotRequest request) {


        String userMessage = request.getMessage();

        // 获取历史记录 (用于发给大模型做上下文)
        List<ChatBotRequest.MessageContext> history = request.getHistory();

        //执行业务逻辑 (RAG搜索 + 调用大模型)
        String aiText = chatService.generateAnswer(userMessage, history);

        // 4. 封装响应
        // 这里返回的对象会被自动序列化为 JSON，结构符合前端 api.ts 的要求
        ChatBotResponse responseData = new ChatBotResponse(aiText, null);

        return Result.success(responseData);
    }





}