package cn.loblok.upc.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ChatBotRequest {

        /**
         * 用户当前输入的问题
         * 对应前端: message
         */
        @Schema(description = "用户当前输入的问题")
        private String message;

        /**
         * 历史对话记录（用于保持上下文）
         * 对应前端: history
         */
        @Schema(description = "历史对话记录（用于保持上下文）")
        private List<MessageContext> history;

        @Data
        @Schema(description = "历史对话记录")
        public static class MessageContext {
            /**
             * 角色: "user" 或 "assistant"
             */
            @Schema(description = "角色: \"user\" 或 \"assistant\"")
            private String role;

            /**
             * 历史消息内容
             */
            @Schema(description = "历史消息内容")
            private String content;
        }

}