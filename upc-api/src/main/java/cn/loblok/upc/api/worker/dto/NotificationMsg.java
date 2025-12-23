package cn.loblok.upc.api.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class NotificationMsg implements Serializable {
    private Long fromUserId;
    private Long toUserId;
    private Long targetId;
    private String type;     // LIKE, COMMENT, SYSTEM, REGISTER
    private String title;
    private String content;
    private Map<String, Object> extra; // 扩展字段，如 postId
}