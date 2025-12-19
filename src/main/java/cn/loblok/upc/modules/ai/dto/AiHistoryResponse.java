package cn.loblok.upc.modules.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AiHistoryResponse {

    //单次会话Id
    private String id;
    //会话标题
    private String title;
//    private String prompt;
    //会话类型
    private String type;
    //会话时间
    private LocalDateTime timestamp;

    //缩略图
    private String thumbnail;

    private List<GenerateMessage> messages;

}