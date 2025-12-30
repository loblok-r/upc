package cn.loblok.upc.auth.dto;

import cn.loblok.upc.common.enums.MessageContentType;
import cn.loblok.upc.common.enums.SenderType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GenerateMessage {

    //消息ID
    @Schema(description = "消息ID")
    private String id;
    //消息发送方1

    @Schema(description = "消息发送方")
    @JsonProperty("sender")
    private SenderType sender;
    //消息内容
    @Schema(description = "消息内容")
    private String content;
    //时间
    @Schema(description = "时间")
    private LocalDateTime timestamp;
    //消息类型
    @Schema(description = "消息类型")
    private MessageContentType type;
    //消息的图片
    @Schema(description = "消息的图片")
    private String imageUrl;

}