package cn.loblok.upc.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatBotRequest {

        /**
         * 用户当前输入的问题
         * 对应前端: message
         */
        private String message;

        /**
         * 历史对话记录（用于保持上下文）
         * 对应前端: history
         */
        private List<MessageContext> history;

        @Data
        public static class MessageContext {
            /**
             * 角色: "user" 或 "assistant"
             */
            private String role;

            /**
             * 历史消息内容
             */
            private String content;
        }

}