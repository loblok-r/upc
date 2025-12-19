package cn.loblok.upc.modules.ai.dto;

import cn.loblok.upc.modules.ai.enums.MessageContentType;
import cn.loblok.upc.modules.ai.enums.SenderType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GenerateMessage {

    //消息ID
    private String id;
    //消息发送方1

    @JsonProperty("sender")
    private SenderType sender;
    //消息内容
    private String content;
    //时间
    private LocalDateTime timestamp;
    //消息类型
    private MessageContentType type;
    //消息的图片
    private String imageUrl;

}