package cn.loblok.upc.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatBotResponse {

        /**
         * AI 的回复内容
         * 前端优先读取: reply
         */
        private String reply;

        /**
         * 额外信息，如 RAG 检索到的参考文档来源
         * 前端暂时没用，但预留
         */
        private List<String> references;

}