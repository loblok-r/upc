package cn.loblok.upc.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AiHistoryResponse {

    //单次会话Id
    @Schema(description = "单次会话Id")
    private String id;
    //会话标题
    @Schema(description = "会话标题")
    private String title;
//    private String prompt;
    //会话类型
    @Schema(description = "会话类型")
    private String type;
    //会话时间
    @Schema(description = "会话时间")
    private LocalDateTime timestamp;

    //缩略图
    @Schema(description = "缩略图")
    private String thumbnail;

    @Schema(description = "会话内容")
    private List<GenerateMessage> messages;

}