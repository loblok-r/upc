package cn.loblok.upc.api.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 秒杀活动信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpTransactionDTO {
    private String tenantId;
    private Long userId;
    private String bizType;      // 或者用 enum，但需确保 MQ 能序列化
    private Long bizId;          // 注意：这里保留为 Long，消费者转 String
    private Integer deltaExps;
    private Integer totalExps;

    private LocalDateTime occurredAt;
}